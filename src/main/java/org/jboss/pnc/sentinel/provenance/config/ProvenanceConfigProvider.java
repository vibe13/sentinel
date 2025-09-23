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
package org.jboss.pnc.sentinel.provenance.config;

import java.util.Map;
import java.util.Optional;

import org.jboss.pnc.sentinel.enums.BuildSystem;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.Getter;

@ApplicationScoped
public class ProvenanceConfigProvider {

    @Inject
    @Getter
    ProvenanceConfig config;

    public String getBuildLogEndpoint(String buildId) {
        return config.pnc().buildLog().endpoint().replace("{id}", buildId);
    }

    public String getAlignmentLogEndpoint(String buildId) {
        return config.pnc().alignmentLog().endpoint().replace("{id}", buildId);
    }

    public Map<String, String> getPncComponentVersions() {
        return config.pnc().builder().components().version();
    }

    public ProvenanceConfig.SlsaConfig.SpecConfig.ProvenanceSpec getProvenanceSlsaSpecs(String slsaSpec) {
        return Optional.ofNullable(config.slsa().spec().specs().get(slsaSpec))
                .orElseThrow(() -> new IllegalStateException("No SLSA spec defined for '" + slsaSpec + "'"));
    }

    public String getBuildSystemBuildType(String buildSystem) {
        if (buildSystem.toLowerCase().equals(BuildSystem.PNC.toName())) {
            return config.pnc().buildType();
        }
        throw new IllegalStateException("No Builder SLSA spec defined for '" + buildSystem + "'");
    }

}
