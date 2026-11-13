# Changelog

[comment]: <> (When bumping [pc:VERSION_LATEST_RELEASE] create a new entry below)
### Unreleased version
- Docs: the README's "Scale replicas" and "Configure read capacity" examples now pass `null` for deletion
  protection, which keeps the current setting. They passed `"disabled"`, which turned off the protection the
  README's create example turns on (#78).
- Fix: an API key with a trailing newline, as a key read from a file or Kubernetes secret often has, now works.
  `PineconeConfig.validate()`, which `Pinecone.Builder.build()` calls, removes leading and trailing whitespace and
  control characters from the key (as `String.trim()` does) and logs a warning that does not include the key.
  Previously `build()` accepted such a key, and every later call threw an `IllegalArgumentException` whose message
  contained the full API key. The trimmed key is used for both REST and gRPC calls, so a key with a trailing tab no
  longer makes gRPC drop the `api-key` header and log the key (#66).
- Changed: `build()` now throws `PineconeConfigurationException` for a key that still contains a character other
  than printable ASCII after trimming, such as a newline inside the key, and for a key made only of whitespace or
  control characters. These keys never worked. The error message does not include the key (#66).

### 6.3.0
- Fix: index connections are no longer shared between `Pinecone` clients that differ in API key, index host,
  TLS setting, source tag or response-metadata listener. Previously a second client opening an index with the
  same name reused the first client's gRPC connection, so its data-plane calls went to the first client's
  project with the first client's API key, and its listener and TLS settings were ignored (#36).
- Changed: `Index.close()` and `AsyncIndex.close()` now release only that object. The shared gRPC connection
  shuts down when the last `Index`/`AsyncIndex` using it is closed; closing one no longer breaks the others.
  Calling `close()` more than once has no further effect.
  - Close every `Index`/`AsyncIndex` you open. An index object that is never closed now keeps its connection
    open even after the others are closed.
  - Don't use an `Index` after closing it; such calls may still succeed while another index object shares the
    connection.
  - Clients share connections only when they use the same listener instance. Building many clients, each with
    a new lambda or method reference as the listener (for example `metrics::record` per client), opens one
    connection per client.
- Fix: `upsert(List<VectorWithUnsignedIndices>, String)` on `Index` and `AsyncIndex` no longer throws
  `NullPointerException` for a `null` namespace; it writes to the default namespace, like the single-vector
  overloads (#37).
- Fix: with `PINECONE_DEBUG=true`, control-plane request logs show the `Api-Key` header as `██` instead of the
  API key. Requests still send the key (#40).
- Bump dependencies to resolve security advisories: Netty 4.1.100.Final to 4.1.137.Final,
  netty-tcnative-boringssl-static 2.0.61.Final to 2.0.77.Final, jackson-core and jackson-databind 2.14.2 to
  2.21.7, Guava 32.0.1-android to 33.7.2-android. `netty-codec-http2`, `netty-handler-proxy`,
  `netty-transport-native-unix-common` and `guava` are now declared as `api` dependencies, so they are published
  at compile scope.

### 6.2.0
- Add readUnits, upsertedCount, and matchedRecords to ResponseMetadata
- Add List operation to ResponseMetadataInterceptor 

### 6.1.0
- Add ResponseMetadataListener for dataplane operation observability

### 6.0.0
- Add support for:
  - dedicated read nodes
  - byoc
  - create namespace and enhance list namespaces
  - update and fetch by metadata
  - metadata indexing
- Generate code based on 2025-10 api spec

### 5.1.0
- Add support for models api

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
- Add support to pass base url for control plane operations

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