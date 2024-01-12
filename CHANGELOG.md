# Changelog

[comment]: <> (When bumping [pc:VERSION_LATEST_RELEASE] create a new entry below)
### Unreleased version
### v0.7.4
- Added source_collection and support to ignore newly added fields to the response body for describeIndex's indexMetaDatabase object

### v0.7.3
- Add assert with retry mechanism
- Add deprecation warning for queries parameter
- Fix path for create and list indexes calls for gcp-stater

### v0.7.2
- Fix extraction of SDK version

### v0.7.0
- Add support to list indexes
- Add support to configure index
- Add user-agent to the header
- Add integration tests for data plane operations

### v0.6.0
- Add async stub for data plane operations
- Add integration tests

### v0.5.1
- Update build.gradle

### v0.5.0
- Update asyncHttpClient with okHttpClient for control plane operations
- Add ability to describe index
- Add apache 2.0 license
- Update gRPC version to 1.58.0

### v0.4.0
- Add support to pass connection url to config objects for data plane operations only

### v0.3.0
- Added ability for index operations to
  - create index using required and optional fields
  - delete index

### v0.2.3
- Update to use latest protos
  - Ability to use SparseValues
  - Filters on describe index stats calls
- Remove vulnerable dependencies

### v0.1.3
This is the first release that will be released to Maven Central. Only build-time functionality is changing in this release.