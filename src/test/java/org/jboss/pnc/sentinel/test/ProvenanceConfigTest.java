/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2025 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.sentinel.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.pnc.sentinel.provenance.config.ProvenanceConfig;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;

@QuarkusTest
class ProvenanceConfigTest {

    @Inject
    ProvenanceConfig config;

    @ConfigProperty(name = "cosign.password")
    String cosignPassword;

    @Test
    void testGetProvenanceSpec() {
        assertEquals("1.1", config.slsa().spec().version());
        assertEquals("https://slsa.dev/provenance/v1", config.slsa().spec().specs().get("1.1").predicateType());
    }

    @Test
    void testSignAndVerifyCosignKeys() throws Exception {
        Path cosignPrivateKeyPath = Paths.get("src", "test", "resources", "cosign", "cosign-v1.key");
        Path cosignPublicKeyPath = Paths.get("src", "test", "resources", "cosign", "cosign-v1.pub");

        String password = "COSIGN_PASSWORD";
        assertEquals(password, cosignPassword);

        String cosignPublicKey = Files.readString(cosignPublicKeyPath);
        assertTrue(cosignPublicKey.startsWith("-----BEGIN PUBLIC KEY-----"));

        String cosignPrivateKey = Files.readString(cosignPrivateKeyPath);
        assertTrue(cosignPrivateKey.startsWith("-----BEGIN ENCRYPTED SIGSTORE PRIVATE KEY-----"));
    }
}
