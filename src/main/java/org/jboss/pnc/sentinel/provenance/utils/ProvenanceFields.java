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

public final class ProvenanceFields {

    private ProvenanceFields() {
    }

    // Common keys
    public static final String BUILD = "build";
    public static final String ENVIRONMENT = "environment";
    public static final String URI = "uri";
    public static final String NAME = "name";

    // SCM
    public static final String SCM_REPOSITORY = "repository";
    public static final String SCM_DOWNSTREAM_REPOSITORY = "downstreamRepository";
    public static final String SCM_COMMIT = "commit";
    public static final String SCM_TAG = "tag";
    public static final String REVISION = "revision";

    // Artifact keys
    public static final String ARTIFACT_IDENTIFIER = "identifier";
    public static final String ARTIFACT_PURL = "purl";
    public static final String ARTIFACT_URI = "uri";
    public static final String ARTIFACT_SHA256 = "sha256";

    // Build details keys
    public static final String BUILD_DETAILS_TYPE = "type";
    public static final String BUILD_DETAILS_TEMPORARY = "temporary";
    public static final String BUILD_DETAILS_SCRIPT = "script";
    public static final String BUILD_DETAILS_NAME = "name";
    public static final String BUILD_DETAILS_BREW_PULL_ACTIVE = "brewPullActive";
    public static final String BUILD_DETAILS_PARAMETERS = "parameters";
    public static final String BUILD_DETAILS_DEFAULT_ALIGN_PARAMETERS = "defaultAlignmentParameters";

    // Byproducts
    public static final String BY_PRODUCTS_BUILD_LOG = "buildLog";
    public static final String BY_PRODUCTS_ALIGNMENT_LOG = "alignmentLog";
}
