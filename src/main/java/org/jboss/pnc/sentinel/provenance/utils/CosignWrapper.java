/*
 * JBoss, Home of Professional Open Source.
 * Copyright ${copyright-years} Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.pnc.sentinel.provenance.utils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * A wrapper around the Cosign CLI for signing blobs with a Cosign private key. Relies on COSIGN_PASSWORD to decrypt the
 * key if encrypted. Once https://github.com/sigstore/sigstore-java will be able to handle Cosign-generated private keys
 * ("-----BEGIN ENCRYPTED SIGSTORE PRIVATE KEY-----"), we might replace this wrapper with proper java code. Bouncycastle
 * is also not good because it does not recognize them as PKCS#8 PEM keys.
 */
public class CosignWrapper {

    private final Path privateKeyPath;
    private final Path publicKeyPath;
    private final String password; // can be "" if key is unencrypted

    public CosignWrapper(Path privateKeyPath, Path publicKeyPath, String password) {
        this.privateKeyPath = privateKeyPath;
        this.publicKeyPath = publicKeyPath;
        this.password = password == null ? "" : password;
    }

    public SignedBlobResult signBlob(String payload) throws IOException, InterruptedException {
        return signBlob(payload.getBytes(), Optional.empty(), Optional.empty(), true);
    }

    public SignedBlobResult signBlob(byte[] payload) throws IOException, InterruptedException {
        return signBlob(payload, Optional.empty(), Optional.empty(), true);
    }

    public SignedBlobResult signBlob(Path blobPath) throws IOException, InterruptedException {
        return signBlob(blobPath, Optional.empty(), Optional.empty(), true);
    }

    public SignedBlobResult signBlob(
            String payload,
            Optional<Path> signaturePath,
            Optional<Path> bundlePath,
            boolean cleanup) throws IOException, InterruptedException {
        return signBlob(payload.getBytes(), signaturePath, bundlePath, cleanup);
    }

    public SignedBlobResult signBlob(
            byte[] payload,
            Optional<Path> signaturePath,
            Optional<Path> bundlePath,
            boolean cleanup) throws IOException, InterruptedException {

        Path tmpBlob = optionallyWriteContentToFile(payload, "cosign-blob-", ".bin");
        try {
            return signBlob(tmpBlob, signaturePath, bundlePath, cleanup);
        } finally {
            Files.deleteIfExists(tmpBlob);
        }
    }

    /**
     * Signs a blob with cosign and returns the raw signature and the .intoto.jsonl bundle.
     */
    public SignedBlobResult signBlob(
            Path blobPath,
            Optional<Path> signaturePath,
            Optional<Path> bundlePath,
            boolean cleanup) throws IOException, InterruptedException {
        Path sigFile = signaturePath.isPresent() ? signaturePath.get() : Files.createTempFile("cosign-sig-", ".sig");
        Path bundleFile = bundlePath.isPresent() ? bundlePath.get()
                : Files.createTempFile("cosign-bundle-", ".intoto.jsonl");

        List<String> commands = List.of(
                "cosign",
                "sign-blob",
                "--yes",
                "--key",
                privateKeyPath.toString(),
                "--output-signature",
                sigFile.toString(),
                "--bundle",
                bundleFile.toString(),
                blobPath.toString());

        ProcessBuilder pb = new ProcessBuilder(commands);
        // pb.redirectErrorStream(true); // merge stderr into stdout
        pb.environment().put("COSIGN_PASSWORD", password != null ? password : "");

        Process p = pb.start();

        // Drain stdout/stderr in separate threads to avoid blocking
        StreamGobbler outGobbler = new StreamGobbler(p.getInputStream(), "cosign-out");
        StreamGobbler errGobbler = new StreamGobbler(p.getErrorStream(), "cosign-err");
        outGobbler.start();
        errGobbler.start();

        // 1. Wait for process to finish
        int exitCode = p.waitFor();

        if (exitCode != 0) {
            String err = Files.readString(errGobbler.capturedFile);
            throw new RuntimeException("cosign failed: " + err);
        }

        try {
            byte[] signature = Files.readAllBytes(sigFile);
            String bundle = Files.readString(bundleFile);

            return new SignedBlobResult(signature, bundle);
        } finally {
            if (cleanup) {
                Files.deleteIfExists(sigFile);
                Files.deleteIfExists(bundleFile);
            }
        }
    }

    /**
     * Verifies a blob using either a detached signature or a bundle.
     *
     * @param payloadFile path of the blob to verify
     * @param signatureFile optional path of the detached raw signature
     * @param bundleFile optional path of the JSONL in-toto bundle
     *
     * @return true if verification succeeds, false otherwise
     */
    public boolean verifyBlob(Path payloadFile, Path signatureFile, Path bundleFile)
            throws IOException, InterruptedException {

        List<String> commands = new ArrayList<>();
        commands.add("cosign");
        commands.add("verify-blob");
        commands.add("--key");
        commands.add(publicKeyPath.toString());

        if (signatureFile != null) {
            commands.add("--signature");
            commands.add(signatureFile.toString());
        }

        if (bundleFile != null) {
            commands.add("--bundle");
            commands.add(bundleFile.toString());
        }

        commands.add(payloadFile.toString());

        ProcessBuilder pb = new ProcessBuilder(commands);
        Process p = pb.start();

        StreamGobbler outGobbler = new StreamGobbler(p.getInputStream(), "cosign-out");
        StreamGobbler errGobbler = new StreamGobbler(p.getErrorStream(), "cosign-err");
        outGobbler.start();
        errGobbler.start();

        int exitCode = p.waitFor();
        return exitCode == 0;
    }

    /**
     * Verifies a blob using either a detached signature or a bundle.
     *
     * @param payload the bytes content of the blob to verify
     * @param signature optional detached raw signature bytes content
     * @param bundle optional JSONL in-toto bundle bytes content
     *
     * @return true if verification succeeds, false otherwise
     */
    public boolean verifyBlob(byte[] payload, byte[] signature, byte[] bundle)
            throws IOException, InterruptedException {

        Path payloadFile = optionallyWriteContentToFile(payload, "cosign-verify-blob-", ".bin");
        Path signatureFile = optionallyWriteContentToFile(signature, "cosign-verify-", ".sig");
        Path bundleFile = optionallyWriteContentToFile(bundle, "cosign-verify-bundle-", ".intoto.jsonl");

        try {
            return verifyBlob(payloadFile, signatureFile, bundleFile);
        } finally {
            Files.deleteIfExists(payloadFile);
            if (signatureFile != null) {
                Files.deleteIfExists(signatureFile);
            }
            if (bundleFile != null) {
                Files.deleteIfExists(bundleFile);
            }
        }
    }

    /**
     * Verifies a blob using either a detached signature or a bundle.
     *
     * @param payload the bytes content of the blob to verify
     * @param signatureFile optional path of the detached raw signature
     * @param bundleFile optional path of the JSONL in-toto bundle
     *
     * @return true if verification succeeds, false otherwise
     */
    public boolean verifyBlob(byte[] payload, Path signatureFile, Path bundleFile)
            throws IOException, InterruptedException {

        Path payloadFile = optionallyWriteContentToFile(payload, "cosign-verify-blob-", ".bin");

        try {
            return verifyBlob(payloadFile, signatureFile, bundleFile);
        } finally {
            Files.deleteIfExists(payloadFile);
        }
    }

    public Path optionallyWriteContentToFile(byte[] content, String prefix, String suffix) throws IOException {
        if (content == null || content.length == 0) {
            return null;
        }
        Path contentFilePath = Files.createTempFile(prefix, suffix);
        Files.write(contentFilePath, content);
        return contentFilePath;

    }

    /** Holds both raw signature and the .intoto.jsonl bundle (a JSONL file containing an in-toto Statement) */
    public record SignedBlobResult(byte[] signature, String bundleJson) {
    }
}
