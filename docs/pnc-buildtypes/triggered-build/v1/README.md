# Build Type: PNC Triggered Build Spec v1
This is a community-maintained [SLSA Provenance](https://slsa.dev/provenance/v1)
`buildType` that describes the execution of a build by Project Newcastle (PNC) reading the configuration provided via source or parameters.

This definition is hosted and maintained by the team maintaining PNC. This definition can be used by tooling thats runs on top of PNC to describe a PNC build.

## Description

```jsonc
"buildType": "https://project-ncl.github.io/pnc-buildtypes/triggered-build/v1"
```

This `buildType` describes the execution of a [Project Newcastle (PNC)][PNC]
build where PNC read the build configuration from a file via a CLI call using [Bacon][Bacon], or via an API call. 


[PNC]: https://orch.pnc.engineering.redhat.com/pnc-web
[Bacon]: https://github.com/project-ncl/bacon

## Build Definition

### External parameters

[External parameters]: #external-parameters

All external parameters are REQUIRED unless otherwise noted.

<table>
<tr><th>Parameter<th>Type<th>Description

<tr id="build.name"><td><code>build.name</code><td>string<td>

The name of the build config.

<tr id="build.description"><td><code>build.description</code><td>string<td>

*OPTIONAL* 

The description of the build config.

<tr id="build.script"><td><code>build.script</code><td>string<td>

The build script to be executed to produce the final binaries.

<tr id="build.type"><td><code>build.type</code><td>string<td>

The build tye. Must be one of `MVN`, `GRADLE`, `NPM`, `SBT`, `MVN_RPM`.

<tr id="build.temporary"><td><code>build.temporary</code><td>boolean<td>

Flag to specify whether the build is persistent (will never be deleted) or temporary (will be deleted after 2 weeks unless used by other builds). This will affect the suffix appended to the final name of the built binaries.

<tr id="environment.id"><td><code>environment.id</code><td>string<td>

The specific `id` of a build environment. 

A build environment is a container where the build will be executed in full isolation from other builds and in network isolation to prevent the download of resources from random locations, apart from the approved ones. 

Many build environments are preassembled and made available for the users to choose, and contain a vast combination of base OS images and tools required to run the build.

<tr id="environment.name"><td><code>environment.name</code><td>string<td>

The `name` of a build environment, e.g. `OpenJDK 1.8; Mvn 3.6.0`. As this will pick the latest up-to-date version of the image, it's the recommended option.

Mutually exclusive with `environment.id`. Exactly one of these fields MUST be set.

<tr id="repository.url"><td><code>repository.url</code><td>string<td>

The URL of the SCM to be checked out. This can be either internal (hosted on private SCM repositories within Red Hat or IBM) or external URL (public GitHub). The source code will be mirrored to an internal SCM repository.

<tr id="repository.revision"><td><code>repository.revision</code><td>string<td>

Git reference within `repository.url` that was checked out, as either
a git ref (e.g. `main`) or a commit SHA (lowercase hex).

<tr id="repository.preBuildSync"><td><code>repository.preBuildSync</code><td>boolean<td>

Flag to specify whether the synchronization from the external repository to the internal repository should happen before each build.

<tr id="parameters.BREW_BUILD_NAME"><td><code>parameters.BREW_BUILD_NAME</code><td>string<td>

*OPTIONAL*

Brew build name of the build configuration. This is used to override the default value, and can be useful for builds that disable PME. For Maven builds the format should be `<groupid>:<artifactid>`.

<tr id="parameters.ALIGNMENT_PARAMETERS"><td><code>parameters.ALIGNMENT_PARAMETERS</code><td>string<td>

*OPTIONAL*

Additional parameters which will be passed to the relevant CLI executable during alignment before the build. The format should be as you would enter them on a command line, and each must start with a dash.

Depending on the `buildType`, one executable among [PME][PME], [GME][GME], [SMEG][SMEG] or [Project Manipulator][Project Manipulator], respectively for `MVN`, `GRADLE`, `SBT` and `NPM` buildTypes.

<tr id="parameters.BUILDER_POD_MEMORY"><td><code>parameters.BUILDER_POD_MEMORY</code><td>number<td>

*OPTIONAL*

Amount of memory the build environment container should have available. Enter number of GiB, for example `4` = 4 GiB, `5.5` = 5632 MiB.

<tr id="parameters.ALIGNMENT_POD_MEMORY"><td><code>parameters.ALIGNMENT_POD_MEMORY</code><td>number<td>

*OPTIONAL*

Amount of memory the alignment container should have available. The format is the same as the `parameters.BUILDER_POD_MEMORY`

<tr id="parameters.BUILD_CATEGORY"><td><code>parameters.BUILD_CATEGORY</code><td>string<td>

*OPTIONAL*

Category of the build. It can be either `SERVICE` for managed service builds or `STANDARD` (default if not present) for on-premise builds. Empty value is not allowed.

<tr id="parameters.EXTRA_REPOSITORIES"><td><code>parameters.EXTRA_REPOSITORIES</code><td>array of strings<td>

*OPTIONAL*

List of public repositories which will be used to proxy build dependencies. Format is a single URL per line.

<tr id="parameters.BREW_PULL_ACTIVE"><td><code>parameters.BREW_PULL_ACTIVE</code><td>boolean<td>

Flag to allow the search for built dependencies in Brew as well as in PNC.

</table>

[PME]: https://github.com/release-engineering/pom-manipulation-ext
[GME]: https://github.com/project-ncl/gradle-manipulator
[SMEG]: https://github.com/project-ncl/smeg
[Project Manipulator]: https://github.com/project-ncl/project-manipulator

### Internal parameters

All internal parameters are OPTIONAL.

| Parameter | Type | Description |
| --------- | ---- | ----------- |
| `defaultAlignmentParameters` | string | The default alignment parameters which will be passed from PNC build system to the relevant CLI executable ([PME][PME], [GME][GME], [SMEG][SMEG] or [Project Manipulator][Project Manipulator]) during alignment before the build. These parameters will be applied in conjunction with the `parameters.ALIGNMENT_PARAMETERS` and will have higher priority (cannot be overridden).|

### Resolved dependencies

The `resolvedDependencies` **MUST** include the following entries:

#### 1. Source Repository Information

- An entry named `repository` that specifies the Git URL and resolved Git commit ID corresponding to the `repository.url` and `repository.revision` values provided by the user.
- An entry named `repository.downstream` that specifies the resolved Git commit ID and URL of the internal SCM repository (within Red Hat or IBM) where the code is hosted after being mirrored from the original `repository.url` and `repository.revision`, and subsequently processed by the alignment workflow.  
- The `repository.downstream` entry **SHOULD** also include an annotation with the commit tag to improve auditability.

---

#### 2. Build Environment Information

- An entry named `environment.uri` that identifies the environment image used to execute the build. This value is derived from the `environment.id` or `environment.name` provided by the user.

---

#### 3. Build-Time Dependencies

- Entries for all additional dependencies that were downloaded during the build process (i.e., build-time dependencies). These dependency entries **SHOULD** include annotations containing the corresponding `identifier`, `purl`, and `uri` values as computed by PNC.


## Run details

### Metadata

The `invocationId` SHOULD be set to the PNC `build.id` unique and immutable identifier.

### Builder

The builder `id` MUST be set to `pnc`. It SHOULD contain a `version` object (map of (string -> string)) with all the PNC service dependencies that are invoked during the build and alignment process, listing the name of such dependencies and their versions.

### ByProducts

The `byproducts` SHOULD contain entries which identify the logs of the alignment process and of the build process. Such entries SHOULD respectively be named `alignmentLog` and `buildLog` and provide the URIs from where the logs can be downloaded.

## Examples

[Examples]: #examples

See [example.json](example.json).

## Version history

### v1

Initial version
