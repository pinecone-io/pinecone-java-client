# Smoke tests

## Mocked critical-path gate (`MockedCriticalPathTest.java`)

A key-free smoke gate that runs the critical **connect → upsert → query** path
against fully in-process mocks. It runs on **every pull request** (the
`Smoke (mocked, no key)` job in `.github/workflows/pr.yml`) with no
`PINECONE_API_KEY`, guarding the request/response plumbing against regressions
before any keyed suite runs.

### What is mocked

Mocks are injected at the transport layer, not at the SDK's public API:

- **Control plane** (`describeIndex`) — REST (OkHttp); mocked with OkHttp
  `MockWebServer` pointed at via `Pinecone.Builder.withHost(...)`.
- **Data plane** (`upsert` / `query`) — gRPC (Netty); mocked with an in-process
  `io.grpc.Server` from `grpc-testing` implementing `VectorServiceGrpc.VectorServiceImplBase`.
  The channel is injected via `PineconeConfig.setCustomManagedChannel`, then
  passed directly to `PineconeConnection` + `Index`.

Everything above the wire — config resolution, request building, proto
marshaling, response deserialization — is the real code path. This is the Java
analogue of the Python SDK's respx-backed gate
(`tests/smoke/test_mocked_critical_path_*.py`), the TS SDK's fetchApi-injected
gate (`src/smoke/mockedCriticalPath.test.ts`), and the Go SDK's in-process
`grpc.Server` gate (`smoke/mocked_critical_path_test.go`).

### Run locally

```sh
./gradlew smokeTest
```

### Zero-collection guard

The `smokeTest` Gradle task is configured to throw `GradleException` if zero
tests execute (`result.testCount == 0`), so an accidentally emptied suite fails
the job instead of passing silently.

### Real (keyed) suites

The keyed integration tests live in `src/integration/` and run in the
`integration-test` job with `PINECONE_API_KEY` from secrets. They are gated
separately from this key-free gate.
