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
package org.jboss.pnc.sentinel.test.utils;

import java.io.IOException;
import java.util.Collection;

import org.jboss.pnc.dto.Artifact;
import org.jboss.pnc.dto.Build;
import org.jboss.pnc.dto.BuildConfigurationRevision;
import org.jboss.pnc.sentinel.PncService;
import org.jboss.pnc.sentinel.errors.ClientException;
import org.jboss.pnc.sentinel.utils.ObjectMapperProvider;
import org.jboss.pnc.sentinel.utils.TestResources;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.enterprise.inject.Alternative;
import jakarta.inject.Singleton;

@Alternative
@Singleton
public class AlternativePncService extends PncService {

    private static final ObjectMapper OBJECT_MAPPER = ObjectMapperProvider.json();

    public AlternativePncService() {
        super("apiUrl");
    }

    public AlternativePncService(String apiUrl) {
        super(apiUrl);
    }

    @Override
    public Build getBuild(String buildId) {
        try {
            return OBJECT_MAPPER.readValue(TestResources.asString("pnc/provenance/build.json"), Build.class);
        } catch (IOException ex) {
            throw new ClientException("Build could not be retrieved because PNC responded with an error", ex);
        }
    }

    @Override
    public BuildConfigurationRevision getBuildConfigRevision(String buildConfigId, Integer buildConfigRevision) {
        try {
            return OBJECT_MAPPER.readValue(
                    TestResources.asString("pnc/provenance/buildConfigRevision.json"),
                    BuildConfigurationRevision.class);
        } catch (IOException ex) {
            throw new ClientException(
                    "BuildConfigRevision could not be retrieved because PNC responded with an error",
                    ex);
        }
    }

    @Override
    public Collection<Artifact> getBuiltArtifacts(String buildID) {
        try {
            return OBJECT_MAPPER
                    .readValue(TestResources.asString("pnc/provenance/builtArtifacts.json"), new TypeReference<>() {
                    });
        } catch (IOException ex) {
            throw new ClientException("Dependencies could not be retrieved because PNC responded with an error", ex);
        }
    }

    @Override
    public Collection<Artifact> getDependencies(String buildID) {
        try {
            return OBJECT_MAPPER.readValue(
                    TestResources.asString("pnc/provenance/buildDependencies_minimal.json"),
                    new TypeReference<>() {
                    });
        } catch (IOException ex) {

            throw new ClientException("Dependencies could not be retrieved because PNC responded with an error", ex);
        }
    }
}