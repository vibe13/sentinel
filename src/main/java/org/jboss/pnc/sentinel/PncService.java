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
package org.jboss.pnc.sentinel;

import java.util.Collection;

import org.jboss.pnc.client.BuildClient;
import org.jboss.pnc.client.BuildConfigurationClient;
import org.jboss.pnc.client.Configuration;
import org.jboss.pnc.client.Configuration.ConfigurationBuilder;
import org.jboss.pnc.client.RemoteResourceException;
import org.jboss.pnc.client.RemoteResourceNotFoundException;
import org.jboss.pnc.dto.Artifact;
import org.jboss.pnc.dto.Build;
import org.jboss.pnc.dto.BuildConfiguration;
import org.jboss.pnc.dto.BuildConfigurationRevision;
import org.jboss.pnc.sentinel.errors.ClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A service to interact with the PNC build system.
 */
public class PncService {

    private static final Logger log = LoggerFactory.getLogger(PncService.class);

    final String apiUrl;

    public String getApiUrl() {
        return apiUrl;
    }

    final BuildClient buildClient;

    final BuildConfigurationClient buildConfigurationClient;

    public PncService(String apiUrl) {
        this.apiUrl = apiUrl;

        buildClient = new BuildClient(getConfiguration());
        buildConfigurationClient = new BuildConfigurationClient(getConfiguration());
    }

    public void close() {
        buildClient.close();
        buildConfigurationClient.close();
    }

    /**
     * Setup basic configuration to be able to talk to PNC.
     *
     * @return the configuration
     */
    public Configuration getConfiguration() {

        ConfigurationBuilder configurationBuilder = Configuration.builder().host(apiUrl).protocol("http");

        return configurationBuilder.build();
    }

    /**
     * <p>
     * Fetch information about the PNC {@link Build} identified by the particular {@code buildId}.
     * </p>
     *
     * <p>
     * In case the {@link Build} with provided identifier cannot be found {@code null} is returned.
     * </p>
     *
     * @param buildId Tbe {@link Build} identifier in PNC
     * @return The {@link Build} object or {@code null} in case the {@link Build} could not be found.
     */
    public Build getBuild(String buildId) {
        log.debug("Fetching Build from PNC with id '{}'", buildId);
        try {
            return buildClient.getSpecific(buildId);
        } catch (RemoteResourceNotFoundException ex) {
            log.warn("Build with id '{}' was not found in PNC", buildId);
            return null;
        } catch (RemoteResourceException ex) {
            throw new ClientException("Build could not be retrieved because PNC responded with an error", ex);
        }
    }

    /**
     * <p>
     * Fetch information about the PNC {@link BuildConfigurationRevision} identified by the particular
     * {@code buildConfigId} and {@code buildConfigRevision}.
     * </p>
     *
     * <p>
     * In case the {@link BuildConfigurationRevision} with provided identifier and revision cannot be found {@code null}
     * is returned.
     * </p>
     *
     * @param buildConfigId The {@link BuildConfiguration} identifier in PNC
     * @param buildConfigRevision The {@link BuildConfiguration} revision in PNC
     * @return The {@link BuildConfigurationRevision} object or {@code null} in case the
     *         {@link BuildConfigurationRevision} could not be found.
     */
    public BuildConfigurationRevision getBuildConfigRevision(String buildConfigId, Integer buildConfigRevision) {
        log.debug(
                "Fetching BuildConfigRevision from PNC with id '{}' and rev '{}'",
                buildConfigId,
                buildConfigRevision);
        try {
            return buildConfigurationClient.getRevision(buildConfigId, buildConfigRevision);
        } catch (RemoteResourceNotFoundException ex) {
            log.warn("BuildConfig with id '{}' and rev '{}' was not found in PNC", buildConfigId, buildConfigRevision);
            return null;
        } catch (RemoteResourceException ex) {
            throw new ClientException(
                    "BuildConfigRevision could not be retrieved because PNC responded with an error",
                    ex);
        }
    }

    /**
     * <p>
     * Fetch all built artifacts of a PNC {@link Build} identified by the particular {@code buildId}.
     * </p>
     *
     * @param buildID Tbe {@link Build} identifier in PNC
     * @return The collection of {@link Artifact} objects or {@code null} in case the {@link Build} could not be found.
     */
    public Collection<Artifact> getBuiltArtifacts(String buildID) {
        log.debug("Fetching all built artifacts from PNC build with id '{}'", buildID);
        try {
            return buildClient.getBuiltArtifacts(buildID).getAll();
        } catch (RemoteResourceException ex) {
            throw new ClientException("Dependencies could not be retrieved because PNC responded with an error", ex);
        }
    }

    /**
     * <p>
     * Fetch all dependencies of a PNC {@link Build} identified by the particular {@code buildId}.
     * </p>
     *
     * @param buildID Tbe {@link Build} identifier in PNC
     * @return The collection of {@link Artifact} objects or {@code null} in case the {@link Build} could not be found.
     */
    public Collection<Artifact> getDependencies(String buildID) {
        log.debug("Fetching all dependencies from PNC build with id '{}'", buildID);
        try {
            return buildClient.getDependencyArtifacts(buildID).getAll();
        } catch (RemoteResourceException ex) {
            throw new ClientException("Dependencies could not be retrieved because PNC responded with an error", ex);
        }
    }
}
