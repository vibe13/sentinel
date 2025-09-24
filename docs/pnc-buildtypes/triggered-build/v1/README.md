# Build Type: PNC Triggered Build
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

<tr id="name"><td><code>name</code><td>string<td>

The name of the build config.

<tr id="description"><td><code>description</code><td>string<td>

*OPTIONAL* 

The description of the build config.

<tr id="buildScript"><td><code>buildScript</code><td>string<td>

The build script to be executed to produce the final binaries.

<tr id="buildType"><td><code>buildType</code><td>string<td>

The build tye. Must be one of `MVN`, `GRADLE`, `NPM`, `SBT`.

<tr id="temporary"><td><code>temporary</code><td>boolean<td>

This flag allows the user to specify whether the build is persistent (will never be deleted) or temporary (will be deleted after 2 weeks unless used by other builds). This will affect the suffix appended to the final name of the built binaries.

<tr id="brewPullActive"><td><code>brewPullActive</code><td>boolean<td>

This flag allows the user to search for built dependencies in Brew as well as in PNC. The number of dependencies in Brew used in current PNC builds is pretty low and the feature slows down both alignment and the build itself. The option enables 

<tr id="environmentId"><td><code>environmentId</code><td>string<td>

The specific `id` of a build environment. 

A build environment is a container where the build will be executed in full isolation from other builds and in network isolation to prevent the download of resources from random locations, apart from the approved ones. 

Many build environments are preassembled and made available for the users to choose, and contain a vast combination of base OS images and tools required to run the build.

<tr id="environmentName"><td><code>environmentName</code><td>string<td>

The `name` of a build environment, e.g. `OpenJDK 1.8; Mvn 3.6.0`. As this will pick the latest up-to-date version of the image, it's the recommended option.

Mutually exclusive with `environmentId`. Exactly one of these fields MUST be set.

<tr id="scm.url"><td><code>scm.url</code><td>string<td>

The URL of the SCM to be checked out. This can be either internal (hosted on private SCM repositories within Red Hat or IBM) or external URL (public GitHub). The source code will be mirrored to an internal SCM repository.

<tr id="scm.revision"><td><code>scm.revision</code><td>string<td>

Git reference within `scm.url` that was checked out, as either
a git ref (e.g. `main`) or a commit SHA (lowercase hex).

<tr id="scm.preBuildSyncEnabled"><td><code>preBuildSyncEnabled</code><td>boolean<td>

Option declaring whether the synchronization (for example adding new commits) from the external repository to the internal repository should happen before each build.

<tr id="parameters.BREW_BUILD_NAME"><td><code>parameters.BREW_BUILD_NAME</code><td>string<td>

*OPTIONAL*

Specify the Brew build name of the build configuration. This is used to override the default value, and can be useful for builds that disable PME. For Maven builds the format should be `<groupid>:<artifactid>`.

<tr id="parameters.ALIGNMENT_PARAMETERS"><td><code>parameters.ALIGNMENT_PARAMETERS</code><td>string<td>

*OPTIONAL*

Additional parameters, which will be passed to the relevant CLI executable during alignment before the build. The format should be as you would enter them on a command line, and each must start with a dash.

Depending on the `buildType`, one executable among [PME][PME], [GME][GME], [SMEG][SMEG] or [Project Manipulator][Project Manipulator], respectively for `MVN`, `GRADLE`, `SBT` and `NPM` buildTypes.

<tr id="parameters.BUILDER_POD_MEMORY"><td><code>parameters.BUILDER_POD_MEMORY</code><td>number<td>

*OPTIONAL*

Specify the amount of memory the build environment container should have available. Enter number of GiB, for example `4` = 4 GiB, `5.5` = 5632 MiB. Use this responsibly and only when needed!

<tr id="parameters.ALIGNMENT_POD_MEMORY"><td><code>parameters.ALIGNMENT_POD_MEMORY</code><td>number<td>

*OPTIONAL*

Specify the amount of memory the alignment container should have available. The format is the same as the `parameters.BUILDER_POD_MEMORY`

<tr id="parameters.BUILD_CATEGORY"><td><code>parameters.BUILD_CATEGORY</code><td>string<td>

*OPTIONAL*

Specify the category of the build. It can be either `SERVICE` for managed service builds or `STANDARD` (default if not present) for on-premise builds. Empty value is not allowed.

<tr id="parameters.EXTRA_REPOSITORIES"><td><code>parameters.EXTRA_REPOSITORIES</code><td>array of strings<td>

*OPTIONAL*

Allows to specify any public repositories, which will be used to proxy build dependencies. Format is a single URL per line.

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

The `resolvedDependencies` MUST contain entries identifying the resolved git commit ID corresponding to `scm.url` and `scm.revision` provided by the user (that MUST be mapped with an entry named `repository`), and the resolved git commit ID and URL corresponding to the internal (to Red Hat or IBM) SCM repository which hosts the code after the mirroring from `scm.url` and `scm.revision` and manipulated by the alignment process (this entry MUST be mapped with an entry named `downstreamRepository` and SHOULD also contain an annotation with the commit TAG for easier auditability). 

The `resolvedDependencies` MUST also contain all the additional dependencies which were downloaded during the build process (build-time dependencies). These additional dependencies SHOULD contain annotations with the corresponding `identifier`, `purl`, `uri` computed by PNC.

## Run details

### Metadata

The `invocationId` SHOULD be set to the PNC `build.id` unique and immutable identifier.

### Builder

The builder `id` MUST be set to `pnc`. It SHOULD contain a `version` object (map of (string -> string)) with all the PNC service dependencies that are invoked during the build and alignment process, listing the name of such dependencies and their versions.

### ByProducts

The `byproducts` SHOULD contain entries which identify the logs of the alignment process and of the build process. Such entries SHOULD respectively be named `alignmentLog` and `buildLog` and provide the URIs from where the logs can be downleaded.

## Examples

[Examples]: #examples

See [example.json](example.json).

## Version history

### v1

Initial version
