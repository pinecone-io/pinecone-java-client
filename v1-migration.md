# Java SDK v1.0.0 Migration Guide

This migration guide is specific to migrating from versions "**v0.8.x**" and below to "**v1.0.x**".

## Changes overview

- Renamed `PineconeControlPlaneClient` to `Pinecone` and added overloaded methods so the users are not required to construct the request objects.
- Added data plane wrappers `Index` and `AsyncIndex` that will eliminate the need of creating Java classes for request objects. `Index` class is for blocking stub while `AsyncIndex` is an async gRPC data plane operations.
- Removed `PineconeClient` and `PineconeConnectionConfig`, and renamed `PineconeClientConfig` to `PineconeConfig` which allows the support to set customer gRPC managed channel for data plane operations along with setting source tag.
- Addressed vulnerabilities by updating the following dependencies:
    - io.grpc:grpc-protobuf: from 1.57.0 to 1.61.0
    - io.grpc:grpc-stub: from 1.57.0 to 1.61.0
    - io.grpc:grpc-netty: from 1.57.0 to 1.61.0
    - com.squareup.okhttp3:okhttp → from 4.10.0 to 4.12.0
- Added the following model classes because the datatype for sparse indices in Pinecone db is `unsigned 32-bit integer` while the proto generated classes were accepting `int` which is `signed 32-bit integers` and before sending it to Pinecone db, it’ll convert the indices to `unsigned 32-bit integers`. The sparse indices will be now accepted as Java `long` instead of Java `int` with the input range of `unsigned 32-bit integer` [0, 2^32 - 1] and everything outside of this range will throw `PineconeValidationException`.
    - QueryResponseWithUnsignedIndices.java
    - ScoredVectorWithUnsignedIndices.java
    - SparseValuesWithUnsignedIndices.java
    - VectorWithUnsignedIndices
- Added read units as a part of queryResponse

## Initialization

The `PineconeControlPlaneClient` is renamed to `Pinecone` class and it now follows a builder pattern which allows to construct PineconeConfig object by default.

**Before: ≤ 0.8.1**

```java
import io.pinecone.*;
import io.pinecone.PineconeControlPlaneClient;
import org.openapitools.client.model.*;

public class InitializeClientExample {
    public static void main(String[] args) {
			PineconeControlPlaneClient controlPlaneClient = 
				new PineconeControlPlaneClient("PINECONE_API_KEY");
		}
}
```

**After: ≥ 1.0.0**

```java
import io.pinecone.clients.Pinecone;
import org.openapitools.client.model.*;

public class InitializeClientExample {
    public static void main(String[] args) {
        Pinecone pinecone = new Pinecone.Builder("PINECONE_API_KEY")
										        .build();
    }
}
```

## Indexes

### Creating indexes

In the new v1.0.0 client release, there is a lot more flexibility in how indexes are created. Users don’t have to construct the CreateIndexRequest object and can pass in the java native data types.

#### Creating a pod index

Prior to v0.8.1, you needed to pass a `CreateIndexRequestSpecPod` object with the `CreateIndexRequest` which needed to be constructed manually. After v1.0.0 you can pass simplified arguments directly to the `createPodsIndex()` function.

**Before: ≤ 0.8.1**

```java
import io.pinecone.*;
import io.pinecone.PineconeControlPlaneClient;
import org.openapitools.client.model.*;
...

PineconeControlPlaneClient controlPlaneClient = 
new PineconeControlPlaneClient("PINECONE_API_KEY");

CreateIndexRequestSpecPod podSpec = new CreateIndexRequestSpecPod()
    .environment("PINECONE_ENVIRONMENT")
    .podType("p1.x1");
CreateIndexRequestSpec createIndexRequestSpec = new CreateIndexRequestSpec()
    .pod(podSpec);
CreateIndexRequest createIndexRequest = new CreateIndexRequest()
    .name("example-index")
    .metric(IndexMetric.COSINE)
    .dimension(10)
    .spec(createIndexRequestSpec);

PineconeControlPlaneClient controlPlaneClient = 
new PineconeControlPlaneClient(apiKey);
IndexModel indexModel = 
controlPlaneClient.createIndex(createIndexRequest);
```

**After: ≥ 1.0.0**

```python
import io.pinecone.clients.Pinecone;
import org.openapitools.client.model.IndexModel;
...
        
Pinecone pinecone = new Pinecone.Builder("PINECONE_API_KEY").build();
        
String indexName = "example-index";
String similarityMetric = "cosine"; // Optional; defaults to cosine similarity
int dimension = 1538;
String environment = "us-east-1-aws";
String podType = "p1.x1";

IndexModel indexModel = 
pinecone.createPodsIndex(indexName, 
    dimension, 
    environment, 
    podType, 
    similarityMetric);

```

#### Creating a serverless index

Prior to v0.8.1, you needed to pass a `ServerlessSpec` object with the `CreateIndexRequest` which needed to be constructed manually. After v1.0.0 you can pass simplified arguments directly to the `createServerlessIndex()` function.

**Before: ≤ 0.8.1**

```java
import io.pinecone.PineconeControlPlaneClient;
import org.openapitools.client.model.*;
...

PineconeControlPlaneClient controlPlaneClient = 
new PineconeControlPlaneClient("PINECONE_API_KEY");

ServerlessSpec serverlessSpec = new ServerlessSpec()
    .cloud(ServerlessSpec.CloudEnum.AWS)
    .region("us-west-2");
CreateIndexRequestSpec createIndexRequestSpec = 
new CreateIndexRequestSpec()
    .serverless(serverlessSpec);
CreateIndexRequest createIndexRequest = new CreateIndexRequest()
    .name("example-index")
    .metric(IndexMetric.COSINE)
    .dimension(10)
    .spec(createIndexRequestSpec);

IndexModel indexModel = 
controlPlaneClient.createIndex(createIndexRequest);
```

**After: ≥ 1.0.0**

```java
import io.pinecone.clients.Pinecone;
import org.openapitools.client.model.IndexModel;
...

Pinecone pinecone = new Pinecone.Builder("PINECONE_API_KEY").build();
        
String indexName = "example-index";
String similarityMetric = "cosine";
int dimension = 1538;
String cloud = "aws";
String region = "us-west-2";

IndexModel indexModel = 
pinecone.createServerlessIndex(indexName, 
    similarityMetric, 
    dimension, 
    cloud, 
    region);
```

### Listing indexes

The `listIndexes` function is unchanged between the two versions.

**Before: ≤ 0.8.1**

```java
import io.pinecone.PineconeControlPlaneClient;
import org.openapitools.client.model.IndexList;
...

PineconeControlPlaneClient controlPlaneClient = 
new PineconeControlPlaneClient("PINECONE_API_KEY");

IndexxList indexes = controlPlaneClient.listIndexes();
```

**After: ≥ 1.0.0**

```java
import io.pinecone.clients.Pinecone;
import org.openapitools.client.model.IndexList;
...

Pinecone pinecone = new Pinecone.Builder("PINECONE_API_KEY").build();

IndexList indexList = controlPlaneClient.listIndexes();
```

### Configuring indexes

Previously you needed to construct the `ConfigureIndexRequest` object and necessary `ConfigureIndexRequestSpec` objects. Now you can pass strings and an int to easily configure a pod index. 

**Before: ≤ 0.8.1**

**Before: ≤ 0.8.1**

```java
import io.pinecone.PineconeControlPlaneClient;
import org.openapitools.client.model.*;
...

PineconeControlPlaneClient controlPlaneClient = 
new PineconeControlPlaneClient("PINECONE_API_KEY");

ConfigureIndexRequestSpecPod pod = new ConfigureIndexRequestSpecPod().replicas(3);
ConfigureIndexRequestSpec spec = new ConfigureIndexRequestSpec().pod(pod);
ConfigureIndexRequest configureIndexRequest = new ConfigureIndexRequest().spec(spec);

controlPlaneClient.configureIndex("my-index", configureIndexRequest);
```

**After: ≥ 1.0.0**

```java
import io.pinecone.clients.Pinecone;
...

Pinecone pinecone = new Pinecone.Builder("PINECONE_API_KEY").build();

String indexName = "my-index";
String podType = "p1.x1";
int newNumberOfReplicas = 7;

pinecone.configureIndex(indexName, podType, newNumberOfReplicas);
```

### Describing indexes

The `describeIndex` function is unchanged between the two versions.

**Before: ≤ 0.8.1**

```java
import io.pinecone.PineconeControlPlaneClient;
import org.openapitools.client.model.IndexModel;
...

PineconeControlPlaneClient controlPlaneClient = 
new PineconeControlPlaneClient("PINECONE_API_KEY");

IndexModel index = controlPlaneClient.describeIndex("my-index");
```

**After: ≥ 1.0.0**

```java
import io.pinecone.clients.Pinecone;
import org.openapitools.client.model.IndexModel;
...

Pinecone pinecone = new Pinecone.Builder("PINECONE_API_KEY").build();

IndexModel index = pinecone.describeIndexx("my-index");
```

### Deleting indexes

The `deleteIndex` function is unchanged between the two versions.

**Before: ≤ 0.8.1**

```java
import io.pinecone.PineconeControlPlaneClient;
...

PineconeControlPlaneClient controlPlaneClient = 
new PineconeControlPlaneClient("PINECONE_API_KEY");

controlPlaneClient.deleteIndex("my-index");
```

**After: ≥ 1.0.0**

```java
import io.pinecone.clients.Pinecone;
...

Pinecone pinecone = new Pinecone.Builder("PINECONE_API_KEY").build();

pinecone.deleteIndex("my-index");
```

## Collections

### Creating collections

Previously you needed to construct the `CreateCollectionRequest` object to pass to the function. Now you can pass two strings denoting the desired collection name and the index source.

**Before: ≤ 0.8.1**

```java
import io.pinecone.PineconeControlPlaneClient;
import org.openapitools.client.model.*;
...

PineconeControlPlaneClient controlPlaneClient = 
new PineconeControlPlaneClient("PINECONE_API_KEY");

CreateCollectionRequest createCollectionRequest = 
new CreateCollectionRequest()
    .name("my-collection")
    .source("my-source-index");
CollectionModel collection = 
controlPlaneClient.createCollection(createCollectionRequest);

CollectionModel collection = controlPlaneClient.createCollection(createCollectionRequest);
```

**After: ≥ 1.0.0**

```java
import io.pinecone.clients.Pinecone;
import org.openapitools.client.model.CollectionModel;
...

Pinecone pinecone = new Pinecone.Builder("PINECONE_API_KEY").build();

String collectionName = "example-collection";
String sourceIndex = "an-index-you-want-a-static-copy-of";

CollectionModel collectionModel = pinecone.createCollection(collectionName, sourceIndex);
```

### Listing collections

The `listCollections` function is unchanged between the two versions.

**Before: ≤ 0.8.1**

```java
import io.pinecone.PineconeControlPlaneClient;
import org.openapitools.client.model.CollectionList;
...

PineconeControlPlaneClient controlPlaneClient = 
new PineconeControlPlaneClient("PINECONE_API_KEY");

CollectionList collections = controlPlaneClient.listCollections();
```

**After: ≥ 1.0.0**

```java
import io.pinecone.clients.Pinecone;
import org.openapitools.client.model.CollectionList;
...

Pinecone pinecone = new Pinecone.Builder("PINECONE_API_KEY").build();

CollectionList collections = pinecone.listCollections();
```

### Describing collections

The `describeCollection` function is unchanged between the two versions.

**Before: ≤ 0.8.1**

```java
import io.pinecone.PineconeControlPlaneClient;
import org.openapitools.client.model.CollectionModel;
...

PineconeControlPlaneClient controlPlaneClient = 
new PineconeControlPlaneClient("PINECONE_API_KEY");

CollectionModel collection = controlPlaneClient.describeCollection("my-collection");
```

**After: ≥ 1.0.0**

```java
import io.pinecone.clients.Pinecone;
import org.openapitools.client.model.CollectionModel;
...

Pinecone pinecone = new Pinecone.Builder("PINECONE_API_KEY").build();

CollectionModel collection = pinecone.describeCollection("my-collection");
```

### Deleting collections

The `deleteCollection` function is unchanged between the two versions.

**Before: ≤ 0.8.1**

```java
import io.pinecone.PineconeControlPlaneClient;
...

PineconeControlPlaneClient controlPlaneClient = 
new PineconeControlPlaneClient("PINECONE_API_KEY");

controlPlaneClient.deleteCollection("my-collection");
```

**After: ≥ 1.0.0**

```java
import io.pinecone.clients.Pinecone;
...

Pinecone pinecone = new Pinecone.Builder("PINECONE_API_KEY").build();

pinecone.deleteCollection("my-collection");
```

## Data Plane operations

### Upsert vectors

**Before: ≤ 0.8.0**

```java
import com.google.common.primitives.Floats;
import com.google.protobuf.Value;
import io.pinecone.*;
import io.pinecone.proto.UpsertRequest;
import io.pinecone.proto.UpsertResponse;
import io.pinecone.proto.Vector;
import io.pinecone.proto.VectorServiceGrpc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

String apiKey = "PINECONE_API_KEY";
String environment = "PINECONE_ENVIRONMENT";
String indexName = "example-index";
String namespace = "example-namespace";
PineconeClientConfig config = new PineconeClientConfig()
        .withApiKey(apiKey)
        .withEnvironment(environment);
PineconeControlPlaneClient controlPlaneClient = new PineconeControlPlaneClient(apiKey);
PineconeClient dataPlaneClient = new PineconeClient(config);
String host = controlPlaneClient.describeIndex(indexName).getHost();

PineconeConnection connection = dataPlaneClient.connect(
        new PineconeConnectionConfig()
                .withConnectionUrl("https://" + host));

VectorServiceGrpc.VectorServiceBlockingStub blockingStub = connection.getBlockingStub();

// upsert
float[][] upsertData = {{1.0F, 2.0F, 3.0F}, {4.0F, 5.0F, 6.0F}, {7.0F, 8.0F, 9.0F}};
List<String> upsertIds = Arrays.asList("v1", "v2", "v3");
List<Vector> upsertVectors = new ArrayList<>();

for (int i = 0; i < upsertData.length; i++) {
    upsertVectors.add(Vector.newBuilder()
            .addAllValues(Floats.asList(upsertData[i]))
            .setId(upsertIds.get(i))
            .build());
}

UpsertRequest request = UpsertRequest.newBuilder()
        .addAllVectors(upsertVectors)
        .setNamespace(namespace)
        .build();

UpsertResponse upsertResponse = blockingStub.upsert(request);
```

**After: ≥ 1.0.0**

```java
import com.google.protobuf.Struct;
import io.pinecone.clients.Index;
import io.pinecone.clients.Pinecone;
import io.pinecone.unsigned_indices_model.VectorWithUnsignedIndices;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static io.pinecone.commons.IndexInterface.buildUpsertVectorWithUnsignedIndices;
...

Pinecone pinecone = new Pinecone.Builder("PINECONE_API_KEY").build();
Index index = pinecone.getIndexConnection("example-index");
// Vector ids to be upserted
List<String> upsertIds = Arrays.asList("v1", "v2", "v3");

// List of values to be upserted
List<List<Float>> values = new ArrayList<>();
values.add(Arrays.asList(1.0f, 2.0f, 3.0f));
values.add(Arrays.asList(4.0f, 5.0f, 6.0f));
values.add(Arrays.asList(7.0f, 8.0f, 9.0f));

List<VectorWithUnsignedIndices> vectors = new ArrayList<>(3);

// Call upsert data
for (int i=0; i<metadataStructList.size(); i++) {
        vectors.add(buildUpsertVectorWithUnsignedIndices(upsertIds.get(i), values.get(i)));
}

index.upsert(vectors, namespace);
```

### Query vectors

**Before: ≤ 0.8.0**

A class for performing query data plane operations with blocking GRPC stub.

```java
import io.pinecone.*;
import io.pinecone.proto.QueryRequest;
import io.pinecone.proto.QueryResponse;
import io.pinecone.proto.VectorServiceGrpc;
...

String apiKey = "PINECONE_API_KEY";
String environment = "PINECONE_ENVIRONMENT";
String indexName = "example-index";
String namespace = "example-namespace";
PineconeClientConfig config = new PineconeClientConfig()
        .withApiKey(apiKey)
        .withEnvironment(environment);
PineconeControlPlaneClient controlPlaneClient = new PineconeControlPlaneClient(apiKey);
PineconeClient dataPlaneClient = new PineconeClient(config);
String host = controlPlaneClient.describeIndex(indexName).getHost();

PineconeConnection connection = dataPlaneClient.connect(
        new PineconeConnectionConfig()
                .withConnectionUrl("https://" + host));

VectorServiceGrpc.VectorServiceBlockingStub blockingStub = connection.getBlockingStub();

QueryRequest queryByIdRequest = QueryRequest.newBuilder()
        .setId("v1")
        .setNamespace(namespace)
        .setTopK(1)
        .setIncludeMetadata(true)
        .setIncludeValues(true)
        .build();

QueryResponse queryResponse = blockingStub.query(queryByIdRequest);
```

**After: ≥ 1.0.0**

A class for performing query data plane operations with blocking GRPC stub.

```java
import io.pinecone.clients.Index;
import io.pinecone.clients.Pinecone;
import io.pinecone.unsigned_indices_model.QueryResponseWithUnsignedIndices;
...
        
Pinecone pinecone = new Pinecone.Builder("PINECONE_API_KEY").build();
Index index = pinecone.getIndexConnection("example-index");
QueryResponseWithUnsignedIndices queryRespone = index.queryByVectorId(3, "v1", "example-namespace");
```

### Fetch vectors

**Before: ≤ 0.8.0**

A class for performing fetch data plane operations with blocking GRPC stub.

```java
import io.pinecone.*;
import io.pinecone.proto.*;
import java.util.Arrays;
import java.util.List;
...

String apiKey = "PINECONE_API_KEY";
String environment = "PINECONE_ENVIRONMENT";
String indexName = "example-index";
String namespace = "example-namespace";
PineconeClientConfig config = new PineconeClientConfig()
        .withApiKey(apiKey)
        .withEnvironment(environment);
PineconeControlPlaneClient controlPlaneClient = new PineconeControlPlaneClient(apiKey);
PineconeClient dataPlaneClient = new PineconeClient(config);
String host = controlPlaneClient.describeIndex(indexName).getHost();

PineconeConnection connection = dataPlaneClient.connect(
        new PineconeConnectionConfig()
                .withConnectionUrl("https://" + host));

VectorServiceGrpc.VectorServiceBlockingStub blockingStub = connection.getBlockingStub();

List<String> ids = Arrays.asList("v1", "v2");
FetchRequest fetchRequest = FetchRequest
							.newBuilder()
							.addAllIds(ids)
							.setNamespace(namespace)
							.build();
FetchResponse fetchResponse = blockingStub.fetch(fetchRequest);
```

**After: ≥ 1.0.0**

A class for performing fetch data plane operations with blocking GRPC stub.

```java
import io.pinecone.clients.Index;
import io.pinecone.clients.Pinecone;
import io.pinecone.proto.FetchResponse;
import java.util.Arrays;
import java.util.List;
...

Pinecone pinecone = new Pinecone.Builder("PINECONE_API_KEY").build();
Index index = pinecone.getIndexConnection("example-index");
List<String> ids = Arrays.asList("v1", "v2", "v3");
FetchResponse fetchResponse = index.fetch(ids, "example-namespace");
```

### Update vectors

**Before: ≤ 0.8.0**

```java
import com.google.common.primitives.Floats;
import io.pinecone.*;
import io.pinecone.proto.UpdateRequest;
import io.pinecone.proto.UpdateResponse;
import io.pinecone.proto.VectorServiceGrpc;
import java.util.List;
...

String apiKey = "PINECONE_API_KEY";
String environment = "PINECONE_ENVIRONMENT";
String indexName = "example-index";
String namespace = "example-namespace";
PineconeClientConfig config = new PineconeClientConfig()
        .withApiKey(apiKey)
        .withEnvironment(environment);
PineconeControlPlaneClient controlPlaneClient = new PineconeControlPlaneClient(apiKey);
PineconeClient dataPlaneClient = new PineconeClient(config);
String host = controlPlaneClient.describeIndex(indexName).getHost();

PineconeConnection connection = dataPlaneClient.connect(
        new PineconeConnectionConfig()
                .withConnectionUrl("https://" + host));

VectorServiceGrpc.VectorServiceBlockingStub blockingStub = connection.getBlockingStub();

List<Float> updateValueList = Floats.asList(10F, 11F, 12F);
UpdateRequest updateRequest = UpdateRequest.newBuilder()
        .setId("v1")
        .setNamespace(namespace)
        .addAllValues(updateValueList)
        .build();
UpdateResponse updateResponse = blockingStub.update(updateRequest);

```

**After: ≥ 1.0.0**

```java
import io.pinecone.clients.Index;
import io.pinecone.clients.Pinecone;
import io.pinecone.proto.UpdateResponse;
import java.util.Arrays;
import java.util.List;
...

Pinecone pinecone = new Pinecone.Builder("PINECONE_API_KEY").build();
Index index = pinecone.getIndexConnection("example-index");
List<Float> values = Arrays.asList(1F, 2F, 3F);
UpdateResponse updateResponse = index.update("v1", values, "example-namespace");
```

### Describe Index Stats

**Before: ≤ 0.8.0**

```java
import io.pinecone.*;
import io.pinecone.proto.*;
...

String apiKey = "PINECONE_API_KEY";
String environment = "PINECONE_ENVIRONMENT";
String indexName = "example-index";
String namespace = "example-namespace";
PineconeClientConfig config = new PineconeClientConfig()
        .withApiKey(apiKey)
        .withEnvironment(environment);
PineconeControlPlaneClient controlPlaneClient = new PineconeControlPlaneClient(apiKey);
PineconeClient dataPlaneClient = new PineconeClient(config);
String host = controlPlaneClient.describeIndex(indexName).getHost();

PineconeConnection connection = dataPlaneClient.connect(
        new PineconeConnectionConfig()
                .withConnectionUrl("https://" + host));

VectorServiceGrpc.VectorServiceBlockingStub blockingStub = connection.getBlockingStub();

DescribeIndexStatsRequest describeIndexStatsRequest = DescribeIndexStatsRequest.newBuilder().build();
DescribeIndexStatsResponse describeIndexStatsResponse = blockingStub.describeIndexStats(describeIndexStatsRequest);

```

**After: ≥ 1.0.0**

```java
import io.pinecone.clients.Index;
import io.pinecone.clients.Pinecone;
import io.pinecone.proto.DescribeIndexStatsResponse;

Pinecone pinecone = new Pinecone.Builder("PINECONE_API_KEY").build();

Index index = pinecone.getIndexConnection("example-index");
DescribeIndexStatsResponse indexStatsResponse = index.describeIndexStats();
```

### Delete vectors

**Before: ≤ 0.8.0**

```java
import io.pinecone.*;
import io.pinecone.proto.*;
import java.util.Arrays;
...

String apiKey = "PINECONE_API_KEY";
String environment = "PINECONE_ENVIRONMENT";
String indexName = "example-index";
String namespace = "example-namespace";
PineconeClientConfig config = new PineconeClientConfig()
        .withApiKey(apiKey)
        .withEnvironment(environment);
PineconeControlPlaneClient controlPlaneClient = new PineconeControlPlaneClient(apiKey);
PineconeClient dataPlaneClient = new PineconeClient(config);
String host = controlPlaneClient.describeIndex(indexName).getHost();

PineconeConnection connection = dataPlaneClient.connect(
        new PineconeConnectionConfig()
                .withConnectionUrl("https://" + host));

VectorServiceGrpc.VectorServiceBlockingStub blockingStub = connection.getBlockingStub();

List<String> ids = Arrays.asList("v1", "v2", "v3");
DeleteRequest deleteRequest = DeleteRequest.newBuilder()
        .setNamespace(namespace)
        .addAllIds(ids)
        .setDeleteAll(false)
        .build();

blockingStub.delete(deleteRequest);
```

**After: ≥ 1.0.0**

```java
import io.pinecone.clients.Index;
import io.pinecone.clients.Pinecone;
import java.util.Arrays;
import java.util.List;
...

Pinecone pinecone = new Pinecone.Builder("PINECONE_API_KEY").build();
Index index = pinecone.getIndexConnection("example-index");
List<String> ids = Arrays.asList("v1", "v2", "v3");
DeleteResponse deleteResponse = index.deleteByIds(ids, "example-namespace");
```