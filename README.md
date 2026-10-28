# Pinecone Java SDK

The Pinecone Java SDK lets JVM applications interact with Pinecone services.

## Requirements

*pinecone-client* requires at least Java 1.8.

## Installation

*pinecone-client* can be installed from [Maven Central](https://mvnrepository.com/artifact/io.pinecone/pinecone-client) for use as a maven dependency in the following ways:

Maven:
```
<dependency>
  <groupId>io.pinecone</groupId>
  <artifactId>pinecone-client</artifactId>
  <version>6.2.0</version>
</dependency>
```

[comment]: <> (^ [pc:VERSION_LATEST_RELEASE])

Gradle:
```
implementation "io.pinecone:pinecone-client:6.2.0"
```

[comment]: <> (^ [pc:VERSION_LATEST_RELEASE])

Alternatively, you can use our standalone uberjar [pinecone-client-6.2.0-all.jar](https://repo1.maven.org/maven2/io/pinecone/pinecone-client/6.2.0/pinecone-client-6.2.0-all.jar), which bundles the Pinecone
SDK and all dependencies together. You can include this in your classpath like you do with any 3rd party JAR without
having to obtain the *pinecone-client* dependencies separately.

[comment]: <> (^ [pc:VERSION_LATEST_RELEASE])

## Usage

### Initializing the client

Before you can use the Pinecone Java SDK, you must sign up for a Pinecone account and find your API key in the Pinecone 
console dashboard at [https://app.pinecone.io](https://app.pinecone.io).

#### Using apiKey

The `Pinecone` class is your main entry point into the Pinecone Java SDK. Build it with your API key. The client does
not read the `PINECONE_API_KEY` environment variable on its own, so pass the value in yourself. Create one `Pinecone`
instance and reuse it, because each instance holds its own HTTP connection pool.

```java
import io.pinecone.clients.Pinecone;

public class InitializeClientExample {
    public static void main(String[] args) {
        Pinecone pinecone = new Pinecone.Builder(System.getenv("PINECONE_API_KEY")).build();
    }
}
```

#### Passing a custom OkHttpClient

To control timeouts or other HTTP settings, pass your own `OkHttpClient` to `withOkHttpClient()`. The client uses it
for control plane calls and for calls made through `getInferenceClient()`. Index connections from
`getIndexConnection()` and `getAsyncIndexConnection()` do not use it. Do not combine it with `withHost()`: when a
custom `OkHttpClient` is set, control plane calls ignore the host you passed.

```java
import io.pinecone.clients.Pinecone;
import okhttp3.OkHttpClient;

import java.util.concurrent.TimeUnit;

public class CustomHttpClientExample {
    public static void main(String[] args) {
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();

        Pinecone pinecone = new Pinecone.Builder(System.getenv("PINECONE_API_KEY"))
                .withOkHttpClient(httpClient)
                .build();
    }
}
```

#### Configuring an HTTP proxy for control plane and inference operations

If your network requires an HTTP proxy, pass its host and port to `withProxy()`. Control plane calls (creating,
describing, and deleting indexes) and calls made through `getInferenceClient()` go through the proxy. Index
connections from `getIndexConnection()` and `getAsyncIndexConnection()` do not use it: upserts, queries, and other
data plane calls connect to the index host directly. You cannot combine `withProxy()` with `withOkHttpClient()`;
`build()` throws a `PineconeConfigurationException` if both are set.

```java
import io.pinecone.clients.Pinecone;
import org.openapitools.db_control.client.model.IndexModel;

public class ProxyExample {
    public static void main(String[] args) {
        Pinecone pinecone = new Pinecone.Builder(System.getenv("PINECONE_API_KEY"))
                .withProxy("proxy.example.com", 8080)
                .build();

        // Sent through the proxy
        IndexModel indexModel = pinecone.describeIndex("product-catalog");
    }
}
```

#### Disabling TLS for data plane operations

To send data plane requests without TLS, for example to a local Pinecone emulator, build the client with
`withTlsEnabled(false)`. Data plane traffic is then unencrypted, so do not disable TLS in production.

```java
import io.pinecone.clients.Index;
import io.pinecone.clients.Pinecone;
import io.pinecone.proto.DescribeIndexStatsResponse;

public class DisableTLSExample {
    public static void main(String[] args) {
        Pinecone pinecone = new Pinecone.Builder(System.getenv("PINECONE_API_KEY"))
                .withHost("http://localhost:5080")
                .withTlsEnabled(false)
                .build();
        Index index = pinecone.getIndexConnection("product-catalog");

        DescribeIndexStatsResponse stats = index.describeIndexStats();
    }
}
```

#### Capturing response metadata for observability

The SDK can report response metadata for upsert, query, fetch, update, delete and list calls made through an index connection. Other calls, such as `describeIndexStats` and `fetchByMetadata`, do not report it. This enables you to track latency metrics and integrate with observability tools like OpenTelemetry, Prometheus, or Datadog.

```java
import io.pinecone.clients.Pinecone;
import io.pinecone.clients.Index;

Pinecone pinecone = new Pinecone.Builder(System.getenv("PINECONE_API_KEY"))
    .withResponseMetadataListener(metadata -> {
        System.out.printf("Operation: %s | Client: %dms | Server: %dms | Network: %dms%n",
            metadata.getOperationName(),
            metadata.getClientDurationMs(),
            metadata.getServerDurationMs(),
            metadata.getNetworkOverheadMs());

        // Log usage and operation-result fields when available
        if (metadata.getReadUnits() != null) {
            System.out.printf("  Read units consumed: %d%n", metadata.getReadUnits());
        }
        if (metadata.getUpsertedCount() != null) {
            System.out.printf("  Upserted count: %d%n", metadata.getUpsertedCount());
        }
        if (metadata.getMatchedRecords() != null) {
            System.out.printf("  Matched records: %d%n", metadata.getMatchedRecords());
        }
    })
    .build();

Index index = pinecone.getIndexConnection("product-catalog");
index.queryByVectorId(3, "article-1", "articles-en");
// Prints, for example: Operation: query | Client: 45ms | Server: 32ms | Network: 13ms
```

The client calls the listener after each of these calls completes, on every index connection obtained from that `Pinecone` instance. An exception thrown by the listener is logged and does not fail the call.

The `ResponseMetadata` object provides:

| Method | Description |
|--------|-------------|
| `getOperationName()` | Operation type: upsert, query, fetch, update, delete, list |
| `getClientDurationMs()` | Total round-trip time measured by the client |
| `getServerDurationMs()` | Server processing time from the `x-pinecone-response-duration-ms` header, or null if the server did not send it |
| `getNetworkOverheadMs()` | Client duration minus server duration, or null if the server duration is unknown |
| `getIndexName()` | Name of the index |
| `getNamespace()` | Namespace used |
| `isSuccess()` | Whether the operation succeeded |
| `getGrpcStatusCode()` | gRPC status code |
| `getErrorType()` | Error category when failed |
| `getReadUnits()` | Read units consumed (available on query, fetch, list) |
| `getUpsertedCount()` | Number of vectors upserted (available on upsert) |
| `getMatchedRecords()` | Number of matched records (available on update) |

For a complete OpenTelemetry integration example with Prometheus and Grafana, see the [java-otel-metrics example](examples/java-otel-metrics/).

# Indexes

Operations related to the building and managing of Pinecone indexes are called [control plane](https://docs.pinecone.io/reference/api/introduction#control-plane) operations.

## Create Index

You can use the Java SDK to create [serverless indexes](https://docs.pinecone.io/guides/indexes/understanding-indexes#serverless-indexes),
[pod-based indexes](https://docs.pinecone.io/guides/indexes/understanding-indexes#pod-based-indexes), and BYOC indexes.

### Create a dense serverless index

The following is an example of creating a serverless index in the `us-west-2` region of AWS. For more information on 
serverless and regional availability, see [Understanding indexes](https://docs.pinecone.io/guides/indexes/understanding-indexes#serverless-indexes).

```java
import io.pinecone.clients.Pinecone;
import org.openapitools.db_control.client.model.IndexModel;
import java.util.HashMap;
...

Pinecone pinecone = new Pinecone.Builder(System.getenv("PINECONE_API_KEY")).build();
        
String indexName = "product-catalog";
String similarityMetric = "cosine";
int dimension = 1536;
String cloud = "aws";
String region = "us-west-2";
HashMap<String, String> tags = new HashMap<>();
tags.put("env", "test");

IndexModel indexModel = pinecone.createServerlessIndex(indexName, similarityMetric, dimension, cloud, region, "enabled", tags);
```

### Create a serverless index with dedicated read capacity

The following example creates a serverless index served by dedicated read nodes. For more information, see [Dedicated Read Nodes](https://docs.pinecone.io/guides/index-data/dedicated-read-nodes).

```java
import io.pinecone.clients.Pinecone;
import org.openapitools.db_control.client.model.IndexModel;
import org.openapitools.db_control.client.model.ReadCapacity;
import org.openapitools.db_control.client.model.ReadCapacityDedicatedSpec;
import org.openapitools.db_control.client.model.ReadCapacityDedicatedConfig;
import org.openapitools.db_control.client.model.ScalingConfigManual;
import java.util.HashMap;
...

Pinecone pinecone = new Pinecone.Builder(System.getenv("PINECONE_API_KEY")).build();
        
String indexName = "product-catalog";
String similarityMetric = "cosine";
int dimension = 1536;
String cloud = "aws";
String region = "us-west-2";
HashMap<String, String> tags = new HashMap<>();
tags.put("env", "test");

// Configure dedicated read capacity with manual scaling
ScalingConfigManual manual = new ScalingConfigManual().shards(2).replicas(2);
ReadCapacityDedicatedConfig dedicated = new ReadCapacityDedicatedConfig()
    .nodeType("t1")
    .scaling("Manual")
    .manual(manual);
ReadCapacity readCapacity = new ReadCapacity(
    new ReadCapacityDedicatedSpec().mode("Dedicated").dedicated(dedicated));

IndexModel indexModel = pinecone.createServerlessIndex(indexName, similarityMetric, dimension, 
    cloud, region, "enabled", tags, readCapacity, null);
```

### Create a serverless index with OnDemand read capacity

OnDemand is the read capacity mode a serverless index gets when you pass `null` for `readCapacity`. The following example sets it explicitly.

```java
import io.pinecone.clients.Pinecone;
import org.openapitools.db_control.client.model.IndexModel;
import org.openapitools.db_control.client.model.ReadCapacity;
import org.openapitools.db_control.client.model.ReadCapacityOnDemandSpec;
import java.util.HashMap;
...

Pinecone pinecone = new Pinecone.Builder(System.getenv("PINECONE_API_KEY")).build();
        
String indexName = "product-catalog";
String similarityMetric = "cosine";
int dimension = 1536;
String cloud = "aws";
String region = "us-west-2";
HashMap<String, String> tags = new HashMap<>();
tags.put("env", "test");

ReadCapacity readCapacity = new ReadCapacity(new ReadCapacityOnDemandSpec().mode("OnDemand"));

IndexModel indexModel = pinecone.createServerlessIndex(indexName, similarityMetric, dimension, 
    cloud, region, "enabled", tags, readCapacity, null);
```

### Create a serverless index with metadata schema

By default, every metadata field is indexed. When you pass a schema, only the fields marked `filterable(true)` are indexed, so only those fields can be used in metadata filters.

```java
import io.pinecone.clients.Pinecone;
import org.openapitools.db_control.client.model.IndexModel;
import org.openapitools.db_control.client.model.BackupModelSchema;
import org.openapitools.db_control.client.model.BackupModelSchemaFieldsValue;
import java.util.HashMap;
...

Pinecone pinecone = new Pinecone.Builder(System.getenv("PINECONE_API_KEY")).build();
        
String indexName = "product-catalog";
String similarityMetric = "cosine";
int dimension = 1536;
String cloud = "aws";
String region = "us-west-2";
HashMap<String, String> tags = new HashMap<>();
tags.put("env", "test");

HashMap<String, BackupModelSchemaFieldsValue> fields = new HashMap<>();
fields.put("genre", new BackupModelSchemaFieldsValue().filterable(true));
fields.put("year", new BackupModelSchemaFieldsValue().filterable(true));
BackupModelSchema schema = new BackupModelSchema().fields(fields);

IndexModel indexModel = pinecone.createServerlessIndex(indexName, similarityMetric, dimension, 
    cloud, region, "enabled", tags, null, schema);
```

### Create a sparse serverless index

The following is an example of creating a sparse serverless index in the `us-east-1` region of AWS. Sparse indexes have no dimension, and the client always creates them with the `dotproduct` metric. For more information, see [Sparse indexes](https://docs.pinecone.io/guides/indexes/sparse-indexes).

```java
import io.pinecone.clients.Pinecone;
import org.openapitools.db_control.client.model.IndexModel;
import java.util.HashMap;
...

Pinecone pinecone = new Pinecone.Builder(System.getenv("PINECONE_API_KEY")).build();
        
String indexName = "product-catalog-sparse";
String cloud = "aws";
String region = "us-east-1";
HashMap<String, String> tags = new HashMap<>();
tags.put("env", "test");
String vectorType = "sparse";

IndexModel indexModel = pinecone.createSparseServelessIndex(indexName, cloud, region, "enabled", tags, vectorType);
```

### Create a pod index

The following is a minimal example of creating a pod-based index. Other `createPodsIndex` overloads also take the metric, replica, shard and pod counts, and a metadata config.

```java
import io.pinecone.clients.Pinecone;
import org.openapitools.db_control.client.model.IndexModel;
...
        
Pinecone pinecone = new Pinecone.Builder(System.getenv("PINECONE_API_KEY")).build();
        
String indexName = "product-catalog";
int dimension = 1536;
String environment = "us-east-1-aws";
String podType = "p1.x1";

IndexModel indexModel = pinecone.createPodsIndex(indexName, dimension, environment, podType);
```

### Create a pod index with deletion protection enabled

The following is an example of creating a pod-based index with deletion protection enabled.

```java
import io.pinecone.clients.Pinecone;
import org.openapitools.db_control.client.model.IndexModel;
...
        
Pinecone pinecone = new Pinecone.Builder(System.getenv("PINECONE_API_KEY")).build();
        
String indexName = "product-catalog";
int dimension = 1536;
String environment = "us-east-1-aws";
String podType = "p1.x1";

IndexModel indexModel = pinecone.createPodsIndex(indexName, dimension, environment, podType, "enabled");
```

### Create a BYOC index

A BYOC (bring your own cloud) index runs in your own cloud account. You need a BYOC environment set up with Pinecone first; you receive its name during BYOC onboarding.

```java
import io.pinecone.clients.Pinecone;
import org.openapitools.db_control.client.model.IndexModel;
...
        
Pinecone pinecone = new Pinecone.Builder(System.getenv("PINECONE_API_KEY")).build();
        
String indexName = "product-catalog";
String similarityMetric = "cosine";
int dimension = 1536;
String byocEnvironment = "aws-us-east-1-b921";

IndexModel indexModel = pinecone.createByocIndex(indexName, similarityMetric, dimension, byocEnvironment);
```

### Create a BYOC index with metadata schema

As with serverless indexes, a schema limits metadata indexing to the fields marked `filterable(true)`.

```java
import io.pinecone.clients.Pinecone;
import org.openapitools.db_control.client.model.IndexModel;
import org.openapitools.db_control.client.model.BackupModelSchema;
import org.openapitools.db_control.client.model.BackupModelSchemaFieldsValue;
import java.util.HashMap;
import java.util.Map;
...
        
Pinecone pinecone = new Pinecone.Builder(System.getenv("PINECONE_API_KEY")).build();
        
String indexName = "product-catalog";
String similarityMetric = "cosine";
int dimension = 1536;
String byocEnvironment = "aws-us-east-1-b921";
HashMap<String, String> tags = new HashMap<>();
tags.put("env", "production");

Map<String, BackupModelSchemaFieldsValue> fields = new HashMap<>();
fields.put("genre", new BackupModelSchemaFieldsValue().filterable(true));
fields.put("year", new BackupModelSchemaFieldsValue().filterable(true));
BackupModelSchema schema = new BackupModelSchema().fields(fields);

IndexModel indexModel = pinecone.createByocIndex(
    indexName, similarityMetric, dimension, byocEnvironment, "enabled", tags, schema);
```

## List indexes

The following example returns every index in your project, with each index's name, dimension, metric, host, status and spec.

```java
import io.pinecone.clients.Pinecone;
import org.openapitools.db_control.client.model.IndexList;
...

Pinecone pinecone = new Pinecone.Builder(System.getenv("PINECONE_API_KEY")).build();
IndexList indexesInYourProject = pinecone.listIndexes();
```

## Describe index

The following example returns the configuration and status of an index.

```java
import io.pinecone.clients.Pinecone;
import org.openapitools.db_control.client.model.IndexModel;
...

Pinecone pinecone = new Pinecone.Builder(System.getenv("PINECONE_API_KEY")).build();
IndexModel indexMetadata = pinecone.describeIndex("product-catalog");
```

## Delete an index

The following example deletes an index and all of its data. This cannot be undone, and the server rejects the request while deletion protection is enabled.

```java
import io.pinecone.clients.Pinecone;
...

Pinecone pinecone = new Pinecone.Builder(System.getenv("PINECONE_API_KEY")).build();
pinecone.deleteIndex("product-catalog");
```

## Scale replicas

The following example changes the number of replicas for a pod-based index. Every `configurePodsIndex` and `configureServerlessIndex` call also sets deletion protection, so pass the value the index should keep.

```java
import io.pinecone.clients.Pinecone;
...

Pinecone pinecone = new Pinecone.Builder(System.getenv("PINECONE_API_KEY")).build();

String indexName = "product-catalog";
int newNumberOfReplicas = 4;
String deletionProtection = "disabled";

pinecone.configurePodsIndex(indexName, newNumberOfReplicas, deletionProtection);
```

## Enable deletion protection for pod index

The following example enables deletion protection for a pod-based index.

```java
import io.pinecone.clients.Pinecone;
...

Pinecone pinecone = new Pinecone.Builder(System.getenv("PINECONE_API_KEY")).build();

String indexName = "product-catalog";
String deletionProtection = "enabled";

pinecone.configurePodsIndex(indexName, deletionProtection);
```

## Enable deletion protection for serverless index

The following example enables deletion protection for a serverless index.

```java
import io.pinecone.clients.Pinecone;
...

Pinecone pinecone = new Pinecone.Builder(System.getenv("PINECONE_API_KEY")).build();

String indexName = "product-catalog";

pinecone.configureServerlessIndex(indexName, "enabled", null, null);
```

### Configure read capacity on an existing serverless index

The following example switches an existing serverless index to Dedicated read capacity with 3 shards and 2 replicas of node type `t1`. To switch back, pass `"OnDemand"` as the read capacity mode and `null` for the node type, shards and replicas.

```java
import io.pinecone.clients.Pinecone;
import org.openapitools.db_control.client.model.IndexModel;
...

Pinecone pinecone = new Pinecone.Builder(System.getenv("PINECONE_API_KEY")).build();

// Parameters: indexName, deletionProtection, tags, embed, readCapacityMode, nodeType, shards, replicas
IndexModel indexModel = pinecone.configureServerlessIndex(
    "product-catalog", "disabled", null, null, "Dedicated", "t1", 3, 2);
```

## Describe index statistics

The following example returns statistics about an index, such as its dimension, total vector count and the vector count of each namespace.

```java
import io.pinecone.clients.Index;
import io.pinecone.clients.Pinecone;
import io.pinecone.proto.DescribeIndexStatsResponse;
...
        
Pinecone pinecone = new Pinecone.Builder(System.getenv("PINECONE_API_KEY")).build();

Index index = pinecone.getIndexConnection("product-catalog");
DescribeIndexStatsResponse indexStatsResponse = index.describeIndexStats();
```

## Upsert vectors

Operations related to the indexing, deleting, and querying of vectors are called [data plane](https://docs.pinecone.io/reference/api/introduction#data-plane) 
operations.

The following example upserts two vectors with metadata into the namespace `articles-en` of `product-catalog`. Each vector
must have as many values as the index dimension (1536 here); upserting an existing ID overwrites that vector.

```java
import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import io.pinecone.clients.Index;
import io.pinecone.clients.Pinecone;
import io.pinecone.proto.UpsertResponse;
import io.pinecone.unsigned_indices_model.VectorWithUnsignedIndices;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import static io.pinecone.commons.IndexInterface.buildUpsertVectorWithUnsignedIndices;
...

Pinecone pinecone = new Pinecone.Builder(System.getenv("PINECONE_API_KEY")).build();
Index index = pinecone.getIndexConnection("product-catalog");

// Placeholder values; use the 1536-value embeddings from your embedding model
List<Float> embedding1 = Collections.nCopies(1536, 0.1f);
List<Float> embedding2 = Collections.nCopies(1536, 0.2f);

Struct metadata1 = Struct.newBuilder()
        .putFields("category", Value.newBuilder().setStringValue("sports").build())
        .putFields("year", Value.newBuilder().setNumberValue(2024).build())
        .build();
Struct metadata2 = Struct.newBuilder()
        .putFields("category", Value.newBuilder().setStringValue("finance").build())
        .putFields("year", Value.newBuilder().setNumberValue(2025).build())
        .build();

List<VectorWithUnsignedIndices> vectors = Arrays.asList(
        buildUpsertVectorWithUnsignedIndices("article-1", embedding1, null, null, metadata1),
        buildUpsertVectorWithUnsignedIndices("article-2", embedding2, null, null, metadata2));
UpsertResponse upsertResponse = index.upsert(vectors, "articles-en");
```

## Query an index

The following example returns the IDs and similarity scores of the 3 vectors in `articles-en` most similar to the
stored vector `article-1`. To query with your own embedding instead, use `queryByVector`.

```java
import io.pinecone.clients.Index;
import io.pinecone.clients.Pinecone;
import io.pinecone.unsigned_indices_model.QueryResponseWithUnsignedIndices;
...
        
Pinecone pinecone = new Pinecone.Builder(System.getenv("PINECONE_API_KEY")).build();
Index index = pinecone.getIndexConnection("product-catalog");
QueryResponseWithUnsignedIndices queryResponse = index.queryByVectorId(3, "article-1", "articles-en");
```

## Delete vectors

The following example deletes vectors by ID.

```java
import io.pinecone.clients.Index;
import io.pinecone.clients.Pinecone;
import io.pinecone.proto.DeleteResponse;
import java.util.Arrays;
import java.util.List;
...

Pinecone pinecone = new Pinecone.Builder(System.getenv("PINECONE_API_KEY")).build();
Index index = pinecone.getIndexConnection("product-catalog");
List<String> ids = Arrays.asList("article-1", "article-2");
DeleteResponse deleteResponse = index.deleteByIds(ids, "articles-en");
```

## Fetch vectors

The following example fetches vectors by ID.

```java
import io.pinecone.clients.Index;
import io.pinecone.clients.Pinecone;
import io.pinecone.proto.FetchResponse;
import java.util.Arrays;
import java.util.List;
...

Pinecone pinecone = new Pinecone.Builder(System.getenv("PINECONE_API_KEY")).build();
Index index = pinecone.getIndexConnection("product-catalog");
List<String> ids = Arrays.asList("article-1", "article-2");
FetchResponse fetchResponse = index.fetch(ids, "articles-en");
```

## List vector IDs

The following example lists the IDs of vectors in `articles-en` that start with the prefix `article-`. Each call
returns one page of IDs (100 by default); other `list` overloads take a `limit` and the pagination token from the
previous response's `getPagination().getNext()`.

```java
import io.pinecone.clients.Index;
import io.pinecone.clients.Pinecone;
import io.pinecone.proto.ListResponse;

Pinecone pinecone = new Pinecone.Builder(System.getenv("PINECONE_API_KEY")).build();
Index index = pinecone.getIndexConnection("product-catalog");
ListResponse listResponse = index.list("articles-en", "article-");
```

## Update vectors

The following example replaces the values of the vector `article-1`.

```java
import io.pinecone.clients.Index;
import io.pinecone.clients.Pinecone;
import io.pinecone.proto.UpdateResponse;
import java.util.Collections;
import java.util.List;
...

Pinecone pinecone = new Pinecone.Builder(System.getenv("PINECONE_API_KEY")).build();
Index index = pinecone.getIndexConnection("product-catalog");
// Placeholder values; use a 1536-value embedding from your embedding model
List<Float> values = Collections.nCopies(1536, 0.3f);
UpdateResponse updateResponse = index.update("article-1", values, "articles-en");
```

## Fetch vectors by metadata

The following example fetches the vectors in `articles-en` whose `category` metadata is `sports`, up to 100 per call.
Pass the previous response's `getPagination().getNext()` to get the next page. The client requires a non-empty
namespace.

```java
import io.pinecone.clients.Index;
import io.pinecone.clients.Pinecone;
import io.pinecone.proto.FetchByMetadataResponse;
import com.google.protobuf.Struct;
import com.google.protobuf.Value;
...

Pinecone pinecone = new Pinecone.Builder(System.getenv("PINECONE_API_KEY")).build();
Index index = pinecone.getIndexConnection("product-catalog");

// {"category": {"$eq": "sports"}}
Struct filter = Struct.newBuilder()
    .putFields("category", Value.newBuilder()
        .setStructValue(Struct.newBuilder()
            .putFields("$eq", Value.newBuilder().setStringValue("sports").build()))
        .build())
    .build();

FetchByMetadataResponse page = index.fetchByMetadata("articles-en", filter, 100, null);

// Fetch the next page, if there is one
if (page.hasPagination() && !page.getPagination().getNext().isEmpty()) {
    page = index.fetchByMetadata("articles-en", filter, 100, page.getPagination().getNext());
}
```

## Update vectors by metadata

The following example sets `reviewed: true` on every vector in `articles-en` whose `category` is `sports`. Fields in the
new metadata are added or overwrite existing values; other metadata fields are kept. Passing `true` as the last argument
(dry run) returns the number of matching vectors without updating them.

```java
import io.pinecone.clients.Index;
import io.pinecone.clients.Pinecone;
import io.pinecone.proto.UpdateResponse;
import com.google.protobuf.Struct;
import com.google.protobuf.Value;
...

Pinecone pinecone = new Pinecone.Builder(System.getenv("PINECONE_API_KEY")).build();
Index index = pinecone.getIndexConnection("product-catalog");

// {"category": {"$eq": "sports"}}
Struct filter = Struct.newBuilder()
    .putFields("category", Value.newBuilder()
        .setStructValue(Struct.newBuilder()
            .putFields("$eq", Value.newBuilder().setStringValue("sports").build()))
        .build())
    .build();

Struct newMetadata = Struct.newBuilder()
    .putFields("reviewed", Value.newBuilder().setBoolValue(true).build())
    .build();

// Dry run: count the matching vectors without updating them
UpdateResponse dryRunResponse = index.updateByMetadata(filter, newMetadata, "articles-en", true);
int matchedRecords = dryRunResponse.getMatchedRecords();

UpdateResponse updateResponse = index.updateByMetadata(filter, newMetadata, "articles-en", false);
```

## Create namespace

The following example creates the namespace `articles-de` with a metadata schema, so only the `category` field is
indexed for filtering. Call `createNamespace("articles-de")` to create it without a schema, which indexes all metadata.

```java
import io.pinecone.clients.Index;
import io.pinecone.clients.Pinecone;
import io.pinecone.proto.MetadataFieldProperties;
import io.pinecone.proto.MetadataSchema;
import io.pinecone.proto.NamespaceDescription;
...

Pinecone pinecone = new Pinecone.Builder(System.getenv("PINECONE_API_KEY")).build();
Index index = pinecone.getIndexConnection("product-catalog");

MetadataSchema schema = MetadataSchema.newBuilder()
    .putFields("category", MetadataFieldProperties.newBuilder().setFilterable(true).build())
    .build();
NamespaceDescription namespaceDescription = index.createNamespace("articles-de", schema);
```

## List namespaces

The following example lists up to 10 namespaces whose names start with `articles-`, then fetches the next page.
`listNamespaces()` with no arguments returns the first page of all namespaces (100 by default). `getTotalCount()`
returns the number of namespaces that match the prefix.

```java
import io.pinecone.clients.Index;
import io.pinecone.clients.Pinecone;
import io.pinecone.proto.ListNamespacesResponse;
...

Pinecone pinecone = new Pinecone.Builder(System.getenv("PINECONE_API_KEY")).build();
Index index = pinecone.getIndexConnection("product-catalog");

ListNamespacesResponse page = index.listNamespaces("articles-", null, 10);
int totalCount = page.getTotalCount();

// Fetch the next page, if there is one
if (page.hasPagination() && !page.getPagination().getNext().isEmpty()) {
    page = index.listNamespaces("articles-", page.getPagination().getNext(), 10);
}
```

## Describe namespace

The following example describes the namespace `articles-en`. The response includes its name, record count and
metadata schema, if it has one.

```java
import io.pinecone.clients.Index;
import io.pinecone.clients.Pinecone;
import io.pinecone.proto.NamespaceDescription;
...

Pinecone pinecone = new Pinecone.Builder(System.getenv("PINECONE_API_KEY")).build();
Index index = pinecone.getIndexConnection("product-catalog");
NamespaceDescription namespaceDescription = index.describeNamespace("articles-en");
```

## Delete namespace

The following example deletes the namespace `articles-de`.

```java
import io.pinecone.clients.Index;
import io.pinecone.clients.Pinecone;
...

Pinecone pinecone = new Pinecone.Builder(System.getenv("PINECONE_API_KEY")).build();
Index index = pinecone.getIndexConnection("product-catalog");
index.deleteNamespace("articles-de");
```

# Collections

A collection is a static copy of a pod-based index. You can create a new pod-based index from it.

## Create collection

The following example creates the collection `product-catalog-backup` from the pod-based index `product-catalog`. The server rejects a serverless or BYOC source index, and a source index that is not ready. The call returns while the collection is still initializing; check its status with `describeCollection`.

```java
import io.pinecone.clients.Pinecone;
import org.openapitools.db_control.client.model.CollectionModel;
...

Pinecone pinecone = new Pinecone.Builder(System.getenv("PINECONE_API_KEY")).build();
CollectionModel collection = pinecone.createCollection("product-catalog-backup", "product-catalog");
```

## List collections

The following example returns the collections in the current project.

```java
import io.pinecone.clients.Pinecone;
import org.openapitools.db_control.client.model.CollectionModel;
import java.util.List;
...

Pinecone pinecone = new Pinecone.Builder(System.getenv("PINECONE_API_KEY")).build();
List<CollectionModel> collections = pinecone.listCollections().getCollections();
```

## Describe a collection

The following example returns the collection `product-catalog-backup`, including its status, dimension, vector count and size.

```java
import io.pinecone.clients.Pinecone;
import org.openapitools.db_control.client.model.CollectionModel;
...

Pinecone pinecone = new Pinecone.Builder(System.getenv("PINECONE_API_KEY")).build();
CollectionModel collection = pinecone.describeCollection("product-catalog-backup");
```

## Delete a collection

The following example deletes the collection `product-catalog-backup`.

```java
import io.pinecone.clients.Pinecone;
...

Pinecone pinecone = new Pinecone.Builder(System.getenv("PINECONE_API_KEY")).build();
pinecone.deleteCollection("product-catalog-backup");
```

# Inference
## Embed

The inference client turns text into embeddings with a model hosted by Pinecone. Set `input_type` to `passage` for text you store and to `query` for search text; `multilingual-e5-large` requires it and returns 1024-value dense embeddings.

```java
import io.pinecone.clients.Inference;
import io.pinecone.clients.Pinecone;
import org.openapitools.inference.client.ApiException;
import org.openapitools.inference.client.model.EmbeddingsList;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
...

Pinecone pinecone = new Pinecone.Builder(System.getenv("PINECONE_API_KEY")).build();
Inference inference = pinecone.getInferenceClient();

List<String> inputs = Arrays.asList(
        "Apple unveiled a new iPhone at its September event.",
        "The city council approved the new park budget.");

Map<String, Object> parameters = new HashMap<>();
parameters.put("input_type", "passage");
parameters.put("truncate", "END");

EmbeddingsList embeddings = inference.embed("multilingual-e5-large", parameters, inputs);

// One embedding per input, in input order
List<Float> firstValues = embeddings.getData().get(0).getDenseEmbedding().getValues();
```

## Rerank

The following example reranks documents by their relevance to a query and returns the two best matches with their scores. The shorter `rerank(model, query, documents)` overload ranks by the `text` field and returns every document.

```java
import io.pinecone.clients.Inference;
import io.pinecone.clients.Pinecone;
import org.openapitools.inference.client.ApiException;
import org.openapitools.inference.client.model.RankedDocument;
import org.openapitools.inference.client.model.RerankResult;

import java.util.*;
...

Pinecone pinecone = new Pinecone.Builder(System.getenv("PINECONE_API_KEY")).build();
Inference inference = pinecone.getInferenceClient();

String query = "The tech company Apple is known for its innovative products like the iPhone.";

List<Map<String, Object>> documents = new ArrayList<>();
String[] texts = {
        "Apple is a popular fruit known for its sweetness and crisp texture.",
        "Apple Inc. has revolutionized the tech industry with its sleek designs.",
        "An apple a day keeps the doctor away, as the saying goes."
};
for (int i = 0; i < texts.length; i++) {
    Map<String, Object> doc = new HashMap<>();
    doc.put("id", "article-" + (i + 1));
    doc.put("body", texts[i]);
    documents.add(doc);
}

// Rank by the "body" field, return the top 2 with their documents
RerankResult result = inference.rerank("bge-reranker-v2-m3", query, documents,
        Arrays.asList("body"), 2, true, new HashMap<>());

for (RankedDocument ranked : result.getData()) {
    System.out.println(ranked.getDocument().get("id") + " " + ranked.getScore());
}
```

## Models

The following example lists the hosted models, first all of them and then filtered by type (`embed` or `rerank`) and, for embedding models, by vector type (`dense` or `sparse`). `describeModel` returns one model's details, such as its default dimension and supported parameters.

```java
import io.pinecone.clients.Inference;
import io.pinecone.clients.Pinecone;
import org.openapitools.inference.client.ApiException;
import org.openapitools.inference.client.model.ModelInfo;
import org.openapitools.inference.client.model.ModelInfoList;
...

Pinecone pinecone = new Pinecone
        .Builder(System.getenv("PINECONE_API_KEY"))
        .build();

Inference inference = pinecone.getInferenceClient();

// List all models
ModelInfoList models = inference.listModels();
System.out.println(models);

// List reranking models
models = inference.listModels("rerank");
System.out.println(models);

// List dense embedding models
models = inference.listModels("embed", "dense");
System.out.println(models);

// Describe one model
ModelInfo modelInfo = inference.describeModel("llama-text-embed-v2");
System.out.println(modelInfo);
```

# Imports
## Start an import

An import loads vectors from Parquet files in object storage (`s3://`, `gs://`, or an Azure Blob Storage `https://` URI) into a serverless index. The import methods are on `AsyncIndex`, but they return once the server responds; the import itself runs in the background, and `startImport` returns its ID. Pass the ID of a storage integration that can read the bucket, or `null` if none is needed. Pass `"continue"` to keep importing past failures, or `null` to use the default, `"abort"`.

```java
import io.pinecone.clients.Pinecone;
import io.pinecone.clients.AsyncIndex;
import org.openapitools.db_data.client.model.StartImportResponse;
...

Pinecone pinecone = new Pinecone.Builder(System.getenv("PINECONE_API_KEY")).build();
AsyncIndex asyncIndex = pinecone.getAsyncIndexConnection("product-catalog");

StartImportResponse response = asyncIndex.startImport("s3://product-catalog-data/vectors/", null, "continue");
String importId = response.getId();
```

## List imports

The following example lists recent and ongoing imports for the index, up to 100 per page. When there are more, `getPagination().getNext()` returns a token to pass to `listImports(limit, paginationToken)`.

```java
import io.pinecone.clients.Pinecone;
import io.pinecone.clients.AsyncIndex;
import org.openapitools.db_data.client.model.ImportModel;
import org.openapitools.db_data.client.model.ListImportsResponse;
...

Pinecone pinecone = new Pinecone.Builder(System.getenv("PINECONE_API_KEY")).build();
AsyncIndex asyncIndex = pinecone.getAsyncIndexConnection("product-catalog");

ListImportsResponse response = asyncIndex.listImports();
for (ImportModel importModel : response.getData()) {
    System.out.println(importModel.getId() + ": " + importModel.getStatus());
}
```

## Describe an import

The following example checks the progress of an import by its ID. The status is `Pending`, `InProgress`, `Completed`, `Failed`, or `Cancelled`; `getError()` explains a failure.

```java
import io.pinecone.clients.Pinecone;
import io.pinecone.clients.AsyncIndex;
import org.openapitools.db_data.client.model.ImportModel;
...

Pinecone pinecone = new Pinecone.Builder(System.getenv("PINECONE_API_KEY")).build();
AsyncIndex asyncIndex = pinecone.getAsyncIndexConnection("product-catalog");

ImportModel importModel = asyncIndex.describeImport("101");
System.out.println(importModel.getStatus() + ", " + importModel.getPercentComplete() + "% complete");
```

## Cancel an import

The following example cancels an import that has not finished.

```java
import io.pinecone.clients.Pinecone;
import io.pinecone.clients.AsyncIndex;
...

Pinecone pinecone = new Pinecone.Builder(System.getenv("PINECONE_API_KEY")).build();
AsyncIndex asyncIndex = pinecone.getAsyncIndexConnection("product-catalog");

asyncIndex.cancelImport("101");
```
# Backups and restore

A backup is a static copy of a serverless index. Pod-based indexes cannot be backed up. `createBackup` returns while the backup is still initializing. Wait until `describeBackup` reports the status `Ready`, then pass the backup ID to `createIndexFromBackup`. That call creates a new serverless index and starts a restore job, which you can track with `describeRestoreJob`. `listProjectBackups()` lists the backups of every index in the project, `listRestoreJobs(limit)` lists restore jobs, and `deleteBackup(backupId)` deletes a backup.

```java
import io.pinecone.clients.Pinecone;
import org.openapitools.db_control.client.model.*;

...

Pinecone pinecone = new Pinecone.Builder(System.getenv("PINECONE_API_KEY")).build();

// The backup name and description are optional
BackupModel backup = pinecone.createBackup("product-catalog", "product-catalog-nightly", "Nightly copy of product-catalog");
String backupId = backup.getBackupId();

String status = backup.getStatus();
while ("Initializing".equals(status)) {
    Thread.sleep(5000);
    status = pinecone.describeBackup(backupId).getStatus();
}
if (!"Ready".equals(status)) {
    throw new IllegalStateException("Backup " + backupId + " is " + status + ", not Ready");
}

// The first 10 backups of one index
for (BackupModel b : pinecone.listIndexBackups("product-catalog").getData()) {
    System.out.println(b.getBackupId() + " " + b.getName() + " " + b.getStatus());
}

CreateIndexFromBackupResponse restore = pinecone.createIndexFromBackup(backupId, "product-catalog-restored");
RestoreJobModel job = pinecone.describeRestoreJob(restore.getRestoreJobId());
System.out.println(job.getTargetIndexName() + " " + job.getStatus());
```

## Examples

Runnable examples are in [examples](examples/): [java-basic-mvn](examples/java-basic-mvn/) upserts a vector and queries it, and [java-otel-metrics](examples/java-otel-metrics/) records response metadata as OpenTelemetry metrics. The integration tests under `src/integration/java/io/pinecone/integration` show more control plane, data plane and inference calls.