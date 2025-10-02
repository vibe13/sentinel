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

import static org.jboss.pnc.sentinel.provenance.utils.ProvenanceFields.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.pnc.dto.Artifact;
import org.jboss.pnc.dto.Build;
import org.jboss.pnc.dto.BuildConfigurationRevision;
import org.jboss.pnc.sentinel.enums.BuildSystem;
import org.jboss.pnc.sentinel.provenance.config.ProvenanceConfig.SlsaConfig.SpecConfig.ProvenanceSpec;
import org.jboss.pnc.sentinel.provenance.config.ProvenanceConfigProvider;
import org.jboss.pnc.sentinel.provenance.model.BuildDefinition;
import org.jboss.pnc.sentinel.provenance.model.Builder;
import org.jboss.pnc.sentinel.provenance.model.Metadata;
import org.jboss.pnc.sentinel.provenance.model.Predicate;
import org.jboss.pnc.sentinel.provenance.model.Provenance;
import org.jboss.pnc.sentinel.provenance.model.ResourceDescriptor;
import org.jboss.pnc.sentinel.provenance.model.RunDetails;

public class ProvenanceUtils {

    /**
     * Convert a collection of artifacts into resource descriptors.
     */
    public static List<ResourceDescriptor> createArtifactsResourceDescriptors(Collection<Artifact> artifacts) {
        return artifacts.stream()
                .map(
                        artifact -> ResourceDescriptor.builder()
                                .name(artifact.getFilename())
                                .digest(Map.of(ARTIFACT_SHA256, artifact.getSha256()))
                                .annotations(
                                        Map.of(
                                                ARTIFACT_IDENTIFIER,
                                                artifact.getIdentifier(),
                                                ARTIFACT_PURL,
                                                artifact.getPurl(),
                                                ARTIFACT_URI,
                                                artifact.getPublicUrl()))
                                .build())
                .toList();
    }

    private static String getBuildSystemBuildType(BuildSystem buildSystem, ProvenanceConfigProvider config) {
        return config.getBuildSystemBuildType(buildSystem.toName());
    }

    public static BuildDefinition createBuildDefinition(
            BuildSystem buildSystem,
            ProvenanceConfigProvider config,
            Map<String, Object> externalParameters,
            Map<String, Object> internalParameters,
            List<ResourceDescriptor> resolvedDependencies) {

        return BuildDefinition.builder()
                .buildType(getBuildSystemBuildType(buildSystem, config))
                .externalParameters(externalParameters)
                .internalParameters(internalParameters)
                .resolvedDependencies(resolvedDependencies)
                .build();
    }

    public static Provenance createProvenance(
            ProvenanceSpec slsaSpec,
            List<ResourceDescriptor> subject,
            Predicate predicate) {
        return Provenance.builder()
                .type(slsaSpec.type())
                .subject(subject)
                .predicateType(slsaSpec.predicateType())
                .predicate(predicate)
                .build();
    }

    public static Provenance createFullPNCBuildProvenance(
            Build pncBuild,
            BuildConfigurationRevision pncBuildConfigRevision,
            Collection<Artifact> builtArtifacts,
            Collection<Artifact> resolvedArtifacts,
            ProvenanceConfigProvider config) {

        List<ResourceDescriptor> subject = createArtifactsResourceDescriptors(builtArtifacts);
        List<ResourceDescriptor> resolvedDependencies = createResolvedDependencies(pncBuild, resolvedArtifacts);

        Map<String, Object> externalParameters = createExternalParameters(pncBuild, pncBuildConfigRevision);
        Map<String, Object> internalParameters = Map
                .of(BUILD_DETAILS_DEFAULT_ALIGN_PARAMETERS, pncBuildConfigRevision.getDefaultAlignmentParams());

        BuildDefinition buildDefinition = createBuildDefinition(
                BuildSystem.PNC,
                config,
                externalParameters,
                internalParameters,
                resolvedDependencies);

        Metadata metadata = Metadata.builder()
                .invocationId(pncBuild.getId())
                .startedOn(pncBuild.getSubmitTime())
                .finishedOn(pncBuild.getEndTime())
                .build();

        Builder builder = Builder.builder()
                .id(BuildSystem.PNC.toName())
                .version(config.getPncComponentVersions())
                .build();

        List<ResourceDescriptor> byproducts = createByproducts(pncBuild, config);

        RunDetails runDetails = RunDetails.builder()
                .builderInfo(builder)
                .metadata(metadata)
                .byproducts(byproducts)
                .build();

        Predicate predicate = Predicate.builder().buildDefinition(buildDefinition).runDetails(runDetails).build();

        return createProvenance(
                config.getProvenanceSlsaSpecs(config.getConfig().slsa().spec().version()),
                subject,
                predicate);
    }

    private static List<ResourceDescriptor> createResolvedDependencies(
            Build pncBuild,
            Collection<Artifact> resolvedArtifacts) {

        var deps = new ArrayList<ResourceDescriptor>();

        deps.add(
                ResourceDescriptor.builder()
                        .name(SCM_REPOSITORY)
                        .digest(Map.of(SCM_COMMIT, pncBuild.getScmBuildConfigRevision()))
                        .uri(pncBuild.getScmRepository().getExternalUrl())
                        .build());

        deps.add(
                ResourceDescriptor.builder()
                        .name(SCM_DOWNSTREAM_REPOSITORY)
                        .digest(Map.of(SCM_COMMIT, pncBuild.getScmRevision()))
                        .uri(pncBuild.getScmUrl())
                        .annotations(Map.of(SCM_TAG, pncBuild.getScmTag()))
                        .build());

        deps.add(
                ResourceDescriptor.builder()
                        .name(ENVIRONMENT)
                        .uri(
                                pncBuild.getEnvironment().getSystemImageRepositoryUrl() + "/"
                                        + pncBuild.getEnvironment().getSystemImageId())
                        .build());

        deps.addAll(createArtifactsResourceDescriptors(resolvedArtifacts));
        return deps;
    }

    private static Map<String, Object> createExternalParameters(Build pncBuild, BuildConfigurationRevision rev) {

        // Merge build parameters and include extra flags
        Map<String, Object> mergedParameters = new HashMap<>(rev.getParameters());
        mergedParameters.put(BUILD_DETAILS_BREW_PULL_ACTIVE, String.valueOf(rev.isBrewPullActive()));

        // Build details map with all relevant metadata
        Map<String, Object> buildDetails = Map.of(
                BUILD_DETAILS_TYPE,
                rev.getBuildType().toString(),
                BUILD_DETAILS_TEMPORARY,
                String.valueOf(pncBuild.getTemporaryBuild()),
                BUILD_DETAILS_SCRIPT,
                rev.getBuildScript(),
                BUILD_DETAILS_NAME,
                rev.getName(),
                BUILD_DETAILS_PARAMETERS,
                mergedParameters);

        return Map.of(
                BUILD,
                buildDetails,
                SCM_REPOSITORY,
                Map.of(
                        URI,
                        pncBuild.getScmRepository().getExternalUrl(),
                        REVISION,
                        rev.getScmRevision(),
                        PRE_BUILD_SYNC,
                        String.valueOf(rev.getScmRepository().getPreBuildSyncEnabled())),
                ENVIRONMENT,
                Map.of(NAME, pncBuild.getEnvironment().getName()));
    }

    private static List<ResourceDescriptor> createByproducts(Build pncBuild, ProvenanceConfigProvider config) {
        return List.of(
                ResourceDescriptor.builder()
                        .name(BY_PRODUCTS_BUILD_LOG)
                        .uri(config.getBuildLogEndpoint(pncBuild.getId()))
                        .build(),
                ResourceDescriptor.builder()
                        .name(BY_PRODUCTS_ALIGNMENT_LOG)
                        .uri(config.getAlignmentLogEndpoint(pncBuild.getId()))
                        .build());
    }
}
