# Testing

The client has three test tiers. Pick the cheapest one that can catch the bug you care about.

| Tier | Source | Gradle task | Needs an API key | Runs in CI |
|---|---|---|---|---|
| Unit and mocked | `src/test` | `./gradlew test` (part of `./gradlew build`) | No | Every PR and push, Java 8, 11, 16 and 17 (`pr.yml`, job `build`) |
| Smoke | `src/integration`, classes tagged `@Tag("smoke")` | `./gradlew smokeTest` | Yes | Every PR and push to `main`, Java 17 (`pr.yml`, job `smoke-test`) |
| Full integration | all of `src/integration` except `@Tag("pod")` classes | `./gradlew integrationTest` | Yes | Nightly, on manual dispatch, and on PRs labelled `full-integration`, Java 8 and 17 (`full-integration.yml`, with pod tests) |
| Pod | `src/integration` classes tagged `@Tag("pod")` | `./gradlew integrationTest -PincludePodTests=true` | Yes | With the full suite in `full-integration.yml` only. Pod indexes count against a small per-project pod quota that parallel PR runs would exhaust |

## Unit and mocked tests

```sh
./gradlew build compileIntegrationTestJava
./gradlew test --tests 'io.pinecone.clients.MockedCriticalPathTest'
```

The first command is the offline check to run before you push. It compiles everything, runs the unit tests, and compiles the integration tests without running them. It takes about 15 seconds.

Tests in `src/test` must not reach the network. To test a request against the client's public API, use the `MockPinecone` JUnit 5 extension in `src/test/java/io/pinecone/helpers`. It serves the control plane over REST and the data plane over gRPC on loopback, records every request, and fails the test when a request has no stubbed route. `MockedCriticalPathTest` and `MockedReadCapacityAndSchemaTest` show how to use it.

## Live tests

Live tests create real indexes in the project that the API key belongs to. Use a disposable project of your own, never a shared or production one. A full run creates several serverless indexes, including ones with dedicated read nodes, and costs real money. Adding `-PincludePodTests=true` also creates pod indexes. A smoke run creates 3 serverless indexes.

```sh
export PINECONE_API_KEY=...   # a key for your own disposable project
./gradlew smokeTest
./gradlew integrationTest --tests 'io.pinecone.integration.inference.EmbedTest'
./gradlew integrationTest
./gradlew integrationTest -PincludePodTests=true --tests 'io.pinecone.integration.dataPlane.UpsertAndQueryPodTest'
```

Run a single class with `--tests` while you work on it. Run `smokeTest` before you open a PR that touches the client, and the full suite only when your change affects collections or other areas that `smokeTest` does not cover. Add `-PincludePodTests=true` only when the change affects pod indexes.

`smokeTest` fails if it selects no tests, so a typo in a tag can't pass silently.

### Environment variables

| Variable | Default | Used for |
|---|---|---|
| `PINECONE_API_KEY` | none, required | Every live test |
| `PINECONE_ENVIRONMENT` | `us-east4-gcp` | Pod indexes |
| `CLOUD` | `aws` | Serverless indexes |
| `REGION` | `us-west-2` | Serverless indexes |
| `DIMENSION` | `4` | Shared test indexes |
| `METRIC` | `dotproduct` | Shared test indexes |
| `GITHUB_RUN_ID` | unset | Set by GitHub Actions. Names the indexes after the CI run so its cleanup job deletes only its own. |

An empty value is treated as unset.

### Cleaning up

Every index and collection the tests create has a name that starts with `ci<run>-<minute>-`. A local run uses `cilocal-`. Tests delete what they create in `@AfterAll`, but an interrupted run can leave indexes behind. To list them and then delete them:

```sh
./gradlew shadowJar
java -cp build/libs/*-all.jar io.pinecone.helpers.IndexCleanupUtility --age-threshold-days 0 --dry-run
java -cp build/libs/*-all.jar io.pinecone.helpers.IndexCleanupUtility --age-threshold-days 0
```

`--age-threshold-days 0` deletes every test-prefixed resource in the project, including those of a run still in progress. Only resources with the test prefix are touched.

## Where a new test belongs

- Request building, response parsing, validation and error mapping: a unit test, with `MockPinecone` if the test goes through `Pinecone` or `Index`.
- Behavior only the real backend has (index readiness, eventual consistency, server-side validation): an integration test. Use the shared indexes from `TestResourcesManager` instead of creating your own where you can.
- A test class that creates or uses a pod index, including the shared pod index from `TestResourcesManager`, gets `@Tag("pod")` so it runs only with `-PincludePodTests=true`.
- Add `@Tag("smoke")` only if the test covers a user-facing operation that the smoke set does not cover yet, creates no pod index, collection or dedicated read node, and keeps the smoke run under a few minutes.
- An integration test that creates an index registers it with `CreatedIndexes` before it creates it, so the index is deleted in `@AfterAll` even if the test fails. Wait for state changes with `Poll`, not `Thread.sleep`.
