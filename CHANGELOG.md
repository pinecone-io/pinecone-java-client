# Changelog

[comment]: <> (When bumping [pc:VERSION_LATEST_RELEASE] create a new entry below)
### Unreleased version
### 5.0.0
- Add support for backups and restore
- Add support for list, describe, and delete namespaces
- Generate code based on 2025-04 api spec
- Automate ndjson handling

### 4.0.1
- Create a new config per index connection 

### 4.0.0
- Add support for sparse indexes
- Generate code based on 2025-01 open-api spec

### 3.1.0
- Add support to pass base url for control and data plane operations

### 3.0.0
- Add support for imports
  - start import
  - list imports
  - describe import
  - cancel import
- Add support for inference
  - embed
  - rerank
- Generate code based on 2024-10 open-api spec

### 2.1.0
- Add support to disable TLS for data plane operations

### 2.0.0
- Add deletion protection

### 1.2.2
- Add support for proxy configuration
- Fix user-agent for grpc

### 1.2.1
- Fix uber jar

### 1.2.0
- Add list with pagination and limit but without prefix
- Add exception cause 

### v1.1.0
- Add list vectors endpoint

### v1.0.0
- Remove vector_service.proto and replace it with the generated classes
- Add data and control plane wrappers
- Refactor configs and PineconeConnection
- Add source tag

### v0.8.0
- Add support for control plane operations for serverless indexes
- Add support for collections for pod indexes

### v0.7.4
- Add source_collection and support to ignore newly added fields to the response body for describeIndex's indexMetaDatabase object

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