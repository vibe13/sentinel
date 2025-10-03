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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.jboss.pnc.sentinel.utils.SchemaValidator;
import org.jboss.pnc.sentinel.utils.SchemaValidator.ValidationResult;
import org.jboss.pnc.sentinel.utils.TestResources;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class ProvenanceValidationTest {

    @Test
    void testValidProvenance() throws IOException {
        String provenance_valid = TestResources.asString("pnc/provenance/files/provenance_valid.json");

        ValidationResult result = SchemaValidator.validate("v1", provenance_valid);
        assertTrue(result.isValid(), "Validation of provenance againt the schema should have passed");
        assertTrue(result.getErrors().isEmpty(), "The errors from the schema validation should have been empty");
    }

    @Test
    void testInvalidProvenanceMissingSubjects() throws IOException {
        String provenance_no_subject_invalid = TestResources
                .asString("pnc/provenance/files/provenance_no_subject_invalid.json");

        ValidationResult result = SchemaValidator.validate("v1", provenance_no_subject_invalid);
        assertFalse(result.isValid(), "Validation of provenance againt the schema should have NOT passed");
        assertFalse(result.getErrors().isEmpty(), "The errors from the schema validation should have been NOT empty");

        assertTrue(
                result.getErrors()
                        .stream()
                        .anyMatch(msg -> msg.contains("Property \"subject\" does not match schema")));
        assertTrue(result.getErrors().stream().anyMatch(msg -> msg.contains("Array has too few items")));
    }

    @Test
    void testInvalidProvenanceMissingExtParam_Environment() throws IOException {
        String provenance_no_ext_param_environment_invalid = TestResources
                .asString("pnc/provenance/files/provenance_no_ext_param_environment_invalid.json");

        ValidationResult result = SchemaValidator.validate("v1", provenance_no_ext_param_environment_invalid);
        assertFalse(result.isValid(), "Validation of provenance againt the schema should have NOT passed");
        assertFalse(result.getErrors().isEmpty(), "The errors from the schema validation should have been NOT empty");

        assertTrue(
                result.getErrors()
                        .stream()
                        .anyMatch(msg -> msg.contains(" Property \"externalParameters\" does not match schema")));
        assertTrue(
                result.getErrors()
                        .stream()
                        .anyMatch(msg -> msg.contains("Instance does not have required property \"environment\"")));
    }

    @Test
    void testInvalidProvenanceMissingResolvedDependencies_DownstreamRepository() throws IOException {
        String provenance_no_resolved_downstream_invalid = TestResources
                .asString("pnc/provenance/files/provenance_no_resolved_downstream_invalid.json");

        ValidationResult result = SchemaValidator.validate("v1", provenance_no_resolved_downstream_invalid);
        assertFalse(result.isValid(), "Validation of provenance againt the schema should have NOT passed");
        assertFalse(result.getErrors().isEmpty(), "The errors from the schema validation should have been NOT empty");

        assertTrue(
                result.getErrors()
                        .stream()
                        .anyMatch(msg -> msg.contains("Property \"resolvedDependencies\" does not match schema")));
        assertTrue(
                result.getErrors()
                        .stream()
                        .anyMatch(msg -> msg.contains("Array does not contain item matching schema")));
    }

    @Test
    void testInvalidProvenanceMissingResolvedDependencies_Environment() throws IOException {
        String provenance_no_resolved_env_invalid = TestResources
                .asString("pnc/provenance/files/provenance_no_resolved_env_invalid.json");

        ValidationResult result = SchemaValidator.validate("v1", provenance_no_resolved_env_invalid);
        assertFalse(result.isValid(), "Validation of provenance againt the schema should have NOT passed");
        assertFalse(result.getErrors().isEmpty(), "The errors from the schema validation should have been NOT empty");

        assertTrue(
                result.getErrors()
                        .stream()
                        .anyMatch(msg -> msg.contains("Property \"resolvedDependencies\" does not match schema")));
        assertTrue(
                result.getErrors()
                        .stream()
                        .anyMatch(msg -> msg.contains("Array does not contain item matching schema")));
    }

    @Test
    void testInvalidProvenanceMissingResolvedDependencies_RepositoryDigest() throws IOException {
        String provenance_no_resolved_repo_digest_invalid = TestResources
                .asString("pnc/provenance/files/provenance_no_resolved_repo_digest_invalid.json");

        ValidationResult result = SchemaValidator.validate("v1", provenance_no_resolved_repo_digest_invalid);
        assertFalse(result.isValid(), "Validation of provenance againt the schema should have NOT passed");
        assertFalse(result.getErrors().isEmpty(), "The errors from the schema validation should have been NOT empty");

        assertTrue(
                result.getErrors()
                        .stream()
                        .anyMatch(msg -> msg.contains("Property \"resolvedDependencies\" does not match schema")));
        assertTrue(
                result.getErrors()
                        .stream()
                        .anyMatch(msg -> msg.contains("Instance does not have required property \"digest\"")));
    }

}
