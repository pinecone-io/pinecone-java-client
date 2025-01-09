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
  <version>3.0.0</version>
</dependency>
```

[comment]: <> (^ [pc:VERSION_LATEST_RELEASE])

Gradle:
```
implementation "io.pinecone:pinecone-client:3.0.0"
```

[comment]: <> (^ [pc:VERSION_LATEST_RELEASE])

Alternatively, you can use our standalone uberjar [pinecone-client-3.0.0-all.jar](https://repo1.maven.org/maven2/io/pinecone/pinecone-client/3.0.0/pinecone-client-3.0.0-all.jar), which bundles the Pinecone
SDK and all dependencies together. You can include this in your classpath like you do with any 3rd party JAR without
having to obtain the *pinecone-client* dependencies separately.

[comment]: <> (^ [pc:VERSION_LATEST_RELEASE])

## Usage

### Initializing the client

Before you can use the Pinecone Java SDK, you must sign up for a Pinecone account and find your API key in the Pinecone 
console dashboard at [https://app.pinecone.io](https://app.pinecone.io).

#### Using apiKey

The `Pinecone` class is your main entry point into the Pinecone Java SDK. You can instantiate the client with 
your `apiKey`, either by passing it as an argument in your code or by setting it as an environment variable called 
`PINECONE_API_KEY`.

This internally instantiates a single shared OkHttpClient instance, which is used for both control plane and inference
operations. Note that the OkHttpClient performs best when you create a single `OkHttpClient` instance and reuse it 
for all of your HTTP calls. This is because each client holds its own connection pool and thread pools. Reusing 
connections and threads reduces latency and saves memory. Conversely, creating a client for each request wastes 
resources on idle pools. More details on the OkHttpClient can be found [here](https://github.com/square/okhttp/blob/f2771425cb714a5b0b27238bd081b2516b4d640f/okhttp/src/main/kotlin/okhttp3/OkHttpClient.kt#L54).

```java
import io.pinecone.clients.Pinecone;

public class InitializeClientExample {
    public static void main(String[] args) {
        Pinecone pinecone = new Pinecone.Builder("PINECONE_API_KEY").build();
    }
}
```

#### Passing OkHttpClient for control plane operations

If you need to provide a custom `OkHttpClient`, you can do so by using the `withOkHttpClient()` method of the 
`Pinecone.Builder` class to pass in your `OkHttpClient` object.

```java
import io.pinecone.clients.Pinecone;
import okhttp3.OkHttpClient;

public class InitializeClientExample {
    public static void main(String[] args) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS);

        OkHttpClient httpClient = builder.build();

        Pinecone pinecone = new Pinecone.Builder("PINECONE_API_KEY").withOkHttpClient(httpClient).build();
    }
}
```

#### Configuring HTTP proxy for both control and data plane operations

If your network setup requires you to interact with Pinecone via a proxy, you will need to pass additional 
configuration using the parameters `host` and `port` of the `ProxyConfig` class.

```java
import io.pinecone.clients.Index;
import io.pinecone.clients.Pinecone;
import io.pinecone.proto.UpsertResponse;
import io.pinecone.unsigned_indices_model.QueryResponseWithUnsignedIndices;
import org.openapitools.db_control.client.model.IndexModel;

import java.util.Arrays;

public class ProxyExample {
    public static void main(String[] args) {
        String apiKey = "PINECONE_API_KEY";
        String proxyHost = "PROXY_HOST";
        int proxyPort = 8080; // Port can be configured based on your setup

        Pinecone pinecone = new Pinecone.Builder(apiKey)
                .withProxy(proxyHost, proxyPort)
                .build();

        // Control plane operation routed through the proxy server
        IndexModel indexModel = pinecone.describeIndex("PINECONE_INDEX");

        // Data plane operations routed through the proxy server
        Index index = pinecone.getIndexConnection("PINECONE_INDEX_NAME");
        // 1. Upsert data
        UpsertResponse upsertResponse = index.upsert("v1", Arrays.asList(1F, 2F, 3F, 4F));
        // 2. Query vector
        QueryResponseWithUnsignedIndices queryResponse = index.queryByVectorId(1, "v1", true, true);
    }
}
```

#### Disabling SSL verification for data plane operations

If you would like to disable TLS verification for data plane operations, you can disable it by setting `enableTLS`
parameter of `PineconeConfig` class to false. We do not recommend going to production with TLS verification disabled.

```java
import io.pinecone.clients.Index;
import io.pinecone.configs.PineconeConfig;
import io.pinecone.configs.PineconeConnection;
import io.pinecone.unsigned_indices_model.QueryResponseWithUnsignedIndices;
import io.pinecone.proto.UpsertResponse;
import java.util.Arrays;

public class DisableTLSExample {
    public static void main(String[] args) {
        PineconeConfig config = new PineconeConfig("api");
        config.setHost("localhost:5081");
        config.setTLSEnabled(false);
        PineconeConnection connection = new PineconeConnection(config);
        Index index = new Index(connection, "example-index");
        
        // Data plane operations
        // 1. Upsert data
        UpsertResponse upsertResponse = index.upsert("v1", Arrays.asList(1f, 2f, 3f));
        // 2. Query data
        QueryResponseWithUnsignedIndices queryResponse = index.queryByVectorId(1, "v1", true, true);
    }
}
```

# Indexes

Operations related to the building and managing of Pinecone indexes are called [control plane](https://docs.pinecone.io/reference/api/introduction#control-plane) operations.

## Create Index

You can use the Java SDK to create two types of indexes: [serverless indexes](https://docs.pinecone.io/guides/indexes/understanding-indexes#serverless-indexes) (recommended for most use cases) and 
[pod-based indexes](https://docs.pinecone.io/guides/indexes/understanding-indexes#pod-based-indexes) (recommended for high-throughput use cases).

### Create a serverless index

The following is an example of creating a serverless index in the `us-west-2` region of AWS. For more information on 
serverless and regional availability, see [Understanding indexes](https://docs.pinecone.io/guides/indexes/understanding-indexes#serverless-indexes).

```java
import io.pinecone.clients.Pinecone;
import org.openapitools.db_control.client.model.IndexModel;
import org.openapitools.db_control.client.model.DeletionProtection;
import java.util.HashMap;
...

Pinecone pinecone = new Pinecone.Builder("PINECONE_API_KEY").build();
        
String indexName = "example-index";
String similarityMetric = "cosine";
int dimension = 1538;
String cloud = "aws";
String region = "us-west-2";
HashMap<String, String> tags = new HashMap<>();
tags.put("env", "test");

IndexModel indexModel = pinecone.createServerlessIndex(indexName, similarityMetric, dimension, cloud, region, DeletionProtection.ENABLED, tags);
```

### Create a pod index

The following is a minimal example of creating a pod-based index. For all the possible configuration options, see 
`main/java/io/pinecone/clients/Pinecone.java`.

```java
import io.pinecone.clients.Pinecone;
import org.openapitools.db_control.client.model.IndexModel;
...
        
Pinecone pinecone = new Pinecone.Builder("PINECONE_API_KEY").build();
        
String indexName = "example-index";
String similarityMetric = "cosine"; // Optional; defaults to cosine similarity
int dimension = 1538;
String environment = "us-east-1-aws";
String podType = "p1.x1";

IndexModel indexModel = pinecone.createPodsIndex(indexName, dimension, environment, podType, similarityMetric);
```

### Create a pod index with deletion protection enabled

The following is an example of creating a pod-based index with deletion protection enabled. For all the possible
configuration options, see `main/java/io/pinecone/clients/Pinecone.java`.

```java
import io.pinecone.clients.Pinecone;
import org.openapitools.client.model.IndexModel;
import org.openapitools.control.client.model.DeletionProtection;
...
        
Pinecone pinecone = new Pinecone.Builder("PINECONE_API_KEY").build();
        
String indexName = "example-index";
int dimension = 1538;
String environment = "us-east-1-aws";
String podType = "p1.x1";

IndexModel indexModel = pinecone.createPodsIndex(indexName, dimension, environment, podType, DeletionProtection.ENABLED);
```

## List indexes

The following example returns all indexes (and their corresponding metadata) in your project.

```java
import io.pinecone.clients.Pinecone;
import org.openapitools.db_control.client.model.IndexList;
...
        
Pinecone pinecone = new Pinecone.Builder("PINECONE_API_KEY").build();
IndexList indexesInYourProject = pinecone.listIndexes();
```

## Describe index

The following example returns metadata about an index.

```java
import io.pinecone.clients.Pinecone;
import org.openapitools.db_control.client.model.IndexModel;
...
        
Pinecone pinecone = new Pinecone.Builder("PINECONE_API_KEY").build(); 
IndexModel indexMetadata = pinecone.describeIndex("example-index");
```

## Delete an index

The following example deletes an index.

```java
import io.pinecone.clients.Pinecone;
...
        
Pinecone pinecone = new Pinecone.Builder("PINECONE_API_KEY").build();
pinecone.deleteIndex("example-index");
```

## Scale replicas

The following example changes the number of replicas for an index.

Note: scaling replicas is only applicable to pod-based indexes.

```java
import io.pinecone.clients.Pinecone;
import org.openapitools.db_control.client.model.DeletionProtection;
...
        
Pinecone pinecone = new Pinecone.Builder("PINECONE_API_KEY").build();

String indexName = "example-index";
String podType = "p1.x1";
int newNumberOfReplicas = 7;
DeletionProtection deletionProtection = DeletionProtection.DISABLED;

pinecone.configurePodsIndex(indexName, podType, newNumberOfReplicas, deletionProtection);
```

## Enable deletion protection for pod index

The following example enables deletion protection for a pod-based index.

```java
import io.pinecone.clients.Pinecone;
import org.openapitools.db_control.client.model.DeletionProtection;
...
        
Pinecone pinecone = new Pinecone.Builder("PINECONE_API_KEY").build();

String indexName = "example-index";
DeletionProtection deletionProtection = DeletionProtection.ENABLED;

pinecone.configurePodsIndex(indexName, deletionProtection);
```

## Enable deletion protection for serverless index

The following example enables deletion protection for a serverless index.

```java
import io.pinecone.clients.Pinecone;
import org.openapitools.db_control.client.model.DeletionProtection;
import java.util.HashMap;
...
        
Pinecone pinecone = new Pinecone.Builder("PINECONE_API_KEY").build();

String indexName = "example-index";
HashMap<String, String> tags = new HashMap<>();
tags.put("env", "test");

pinecone.configureServerlessIndex(indexName, DeletionProtection.ENABLED, tags);
```

## Describe index statistics

The following example returns statistics about an index.

```java
import io.pinecone.clients.Index;
import io.pinecone.clients.Pinecone;
import io.pinecone.proto.DescribeIndexStatsResponse;
...
        
Pinecone pinecone = new Pinecone.Builder("PINECONE_API_KEY").build();

Index index = pinecone.getIndexConnection("example-index");
DescribeIndexStatsResponse indexStatsResponse = index.describeIndexStats();
```

## Upsert vectors

Operations related to the indexing, deleting, and querying of vectors are called [data plane](https://docs.pinecone.io/reference/api/introduction#data-plane) 
operations.

The following example upserts vectors to `example-index`.

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

// List of sparse indices to be upserted
List<List<Long>> sparseIndices = new ArrayList<>();
sparseIndices.add(Arrays.asList(1L, 2L, 3L));
sparseIndices.add(Arrays.asList(4L, 5L, 6L));
sparseIndices.add(Arrays.asList(7L, 8L, 9L));

// List of sparse values to be upserted
List<List<Float>> sparseValues = new ArrayList<>();
sparseValues.add(Arrays.asList(1000f, 2000f, 3000f));
sparseValues.add(Arrays.asList(4000f, 5000f, 6000f));
sparseValues.add(Arrays.asList(7000f, 8000f, 9000f));

List<VectorWithUnsignedIndices> vectors = new ArrayList<>(3);

// metadata to be upserted
Struct metadataStruct1 = Struct.newBuilder()
.putFields("genre", Value.newBuilder().setStringValue("action").build())
.putFields("year", Value.newBuilder().setNumberValue(2019).build())
.build();

Struct metadataStruct2 = Struct.newBuilder()
.putFields("genre", Value.newBuilder().setStringValue("thriller").build())
.putFields("year", Value.newBuilder().setNumberValue(2020).build())
.build();

Struct metadataStruct3 = Struct.newBuilder()
.putFields("genre", Value.newBuilder().setStringValue("comedy").build())
.putFields("year", Value.newBuilder().setNumberValue(2021).build())
.build();
List<Struct> metadataStructList = Arrays.asList(metadataStruct1, metadataStruct2, metadataStruct3);

// Upsert data
for (int i=0; i<metadataStructList.size(); i++) {
    vectors.add(buildUpsertVectorWithUnsignedIndices(upsertIds.get(i), values.get(i), sparseIndices.get(i), sparseValues.get(i), metadataStructList.get(i)));
}
UpsertResponse upsertResponse = index.upsert(vectors, "example-namespace");
```

## Query an index

The following example queries the index `example-index` with metadata
filtering.

```java
import io.pinecone.clients.Index;
import io.pinecone.clients.Pinecone;
import io.pinecone.unsigned_indices_model.QueryResponseWithUnsignedIndices;
...
        
Pinecone pinecone = new Pinecone.Builder("PINECONE_API_KEY").build();
Index index = pinecone.getIndexConnection("example-index");
QueryResponseWithUnsignedIndices queryRespone = index.queryByVectorId(3, "v1", "example-namespace");
```

## Delete vectors

The following example deletes vectors by ID.

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

## Fetch vectors

The following example fetches vectors by ID.

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

## List vector IDs

The following example lists up to 100 vector IDs from a Pinecone index.

This method accepts optional parameters for `namespace`, `prefix`, `limit`, and `paginationToken`. 

The following demonstrates how to use the `list` endpoint to get vector IDs from a specific `namespace`, filtered by a 
given `prefix`.

```java
import io.pinecone.clients.Index;
import io.pinecone.clients.Pinecone;
import io.pinecone.proto.ListResponse;

Pinecone pinecone = new Pinecone.Builder(System.getenv("PINECONE_API_KEY")).build();
String indexName = "example-index";
Index index = pinecone.getIndexConnection(indexName);
ListResponse listResponse = index.list("example-namespace", "prefix-");
```

## Update vectors

The following example updates vectors by ID.

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

# Collections

Collections fall under control plane operations.

## Create collection

The following example creates the collection `example-collection` from
`example-index`.

```java
import io.pinecone.clients.Pinecone;
import org.openapitools.db_control.client.model.CollectionModel;
...

Pinecone pinecone = new Pinecone.Builder("PINECONE_API_KEY").build();
String collectionName = "example-collection";
String sourceIndex = "an-index-you-want-a-static-copy-of";

CollectionModel collectionModel = pinecone.createCollection(collectionName, sourceIndex);
```

## List collections

The following example returns a list of the collections in the current project.

```java
import io.pinecone.clients.Pinecone;
import org.openapitools.db_control.client.model.CollectionModel;
import java.util.List;
...

Pinecone pinecone = new Pinecone.Builder("PINECONE_API_KEY").build();
List<CollectionModel> collectionModels = pinecone.listCollections().getCollections();
```

## Describe a collection

The following example returns a description of the collection

```java
import io.pinecone.clients.Pinecone;
import org.openapitools.db_control.client.model.CollectionModel;
...
        
Pinecone pinecone = new Pinecone.Builder("PINECONE_API_KEY").build();
CollectionModel model = pinecone.describeCollection("example-collection");
```

## Delete a collection

The following example deletes the collection `example-collection`.

```java
import io.pinecone.clients.Pinecone;
...
        
Pinecone pinecone = new Pinecone.Builder("PINECONE_API_KEY").build();
pinecone.deleteCollection("example-collection");
```

# Inference
## Embed
The Pinecone SDK now supports creating embeddings via the [Inference API](https://docs.pinecone.io/guides/inference/understanding-inference).

```java
import io.pinecone.clients.Inference;
import io.pinecone.clients.Pinecone;
import org.openapitools.inference.client.ApiException;
import org.openapitools.inference.client.model.Embedding;
import org.openapitools.inference.client.model.EmbeddingsList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
...

Pinecone pinecone = new Pinecone.Builder("PINECONE_API_KEY").build();
Inference inference = pinecone.getInferenceClient();

// Prepare input sentences to be embedded
List<String> inputs = new ArrayList<>();
inputs.add("The quick brown fox jumps over the lazy dog.");
inputs.add("Lorem ipsum");

// Specify the embedding model and parameters
String embeddingModel = "multilingual-e5-large";

Map<String, Object> parameters = new HashMap<>();
parameters.put("input_type", "query");
parameters.put("truncate", "END");

// Generate embeddings for the input data
EmbeddingsList embeddings = inference.embed(embeddingModel, parameters, inputs);

// Get embedded data
List<Embedding> embeddedData = embeddings.getData();
```

## Rerank
The following example shows how to rerank items according to their relevance to a query.

```java
import io.pinecone.clients.Inference;
import io.pinecone.clients.Pinecone;
import org.openapitools.inference.client.ApiException;
import org.openapitools.inference.client.model.RerankResult;

import java.util.*;
...

Pinecone pinecone = new Pinecone.Builder(System.getenv("PINECONE_API_KEY")).build();
Inference inference = pinecone.getInferenceClient();

// The model to use for reranking
String model = "bge-reranker-v2-m3";

// The query to rerank documents against
String query = "The tech company Apple is known for its innovative products like the iPhone.";

// Add the documents to rerank
List<Map<String, String>> documents = new ArrayList<>();
Map<String, String> doc1 = new HashMap<>();
doc1.put("id", "vec1");
doc1.put("my_field", "Apple is a popular fruit known for its sweetness and crisp texture.");
documents.add(doc1);

Map<String, String> doc2 = new HashMap<>();
doc2.put("id", "vec2");
doc2.put("my_field", "Many people enjoy eating apples as a healthy snack.");
documents.add(doc2);

Map<String, String> doc3 = new HashMap<>();
doc3.put("id", "vec3");
doc3.put("my_field", "Apple Inc. has revolutionized the tech industry with its sleek designs and user-friendly interfaces.");
documents.add(doc3);

Map<String, String> doc4 = new HashMap<>();
doc4.put("id", "vec4");
doc4.put("my_field", "An apple a day keeps the doctor away, as the saying goes.");
documents.add(doc4);

// The fields to rank the documents by. If not provided, the default is "text"
List<String> rankFields = Arrays.asList("my_field");

// The number of results to return sorted by relevance. Defaults to the number of inputs
int topN = 2;

// Whether to return the documents in the response
boolean returnDocuments = true;

// Additional model-specific parameters for the reranker
Map<String, String> parameters = new HashMap<>();
parameters.put("truncate", "END");

// Send ranking request
RerankResult result = inference.rerank(model, query, documents, rankFields, topN, returnDocuments, parameters);

// Get ranked data
System.out.println(result.getData());
```

# Imports
## Start an import

The following example initiates an asynchronous import of vectors from object storage into the index.

```java
import io.pinecone.clients.Pinecone;
import io.pinecone.clients.AsyncIndex;
import org.openapitools.db_data.client.ApiException;
import org.openapitools.db_data.client.model.ImportErrorMode;
import org.openapitools.db_data.client.model.StartImportResponse;
...

// Initialize pinecone object
Pinecone pinecone = new Pinecone.Builder("PINECONE_API_KEY").build();
// Get async imports connection object
AsyncIndex asyncIndex = pinecone.getAsyncIndexConnection("PINECONE_INDEX_NAME");

// s3 uri
String uri = "s3://path/to/file.parquet";

// Start an import
StartImportResponse response = asyncIndex.startImport(uri, "123-456-789", ImportErrorMode.OnErrorEnum.CONTINUE);
```

## List imports

The following example lists all recent and ongoing import operations for the specified index.

```java
import io.pinecone.clients.Pinecone;
import io.pinecone.clients.AsyncIndex;
import org.openapitools.db_data.client.ApiException;
import org.openapitools.db_data.client.model.ListImportsResponse;
...

// Initialize pinecone object
Pinecone pinecone = new Pinecone.Builder("PINECONE_API_KEY").build();
// Get async imports connection object
AsyncIndex asyncIndex = pinecone.getAsyncIndexConnection("PINECONE_INDEX_NAME");

// List imports
ListImportsResponse response = asyncIndex.listImports(100, "some-pagination-token");
```

## Describe an import

The following example retrieves detailed information about a specific import operation using its unique identifier.

```java
import io.pinecone.clients.Pinecone;
import io.pinecone.clients.AsyncIndex;
import org.openapitools.db_data.client.ApiException;
import org.openapitools.db_data.client.model.ImportModel;
...

// Initialize pinecone object
Pinecone pinecone = new Pinecone.Builder("PINECONE_API_KEY").build();
// Get async imports connection object
AsyncIndex asyncIndex = pinecone.getAsyncIndexConnection("PINECONE_INDEX_NAME");
        
// Describe import
ImportModel importDetails = asyncIndex.describeImport("1");
```

## Cancel an import

The following example attempts to cancel an ongoing import operation using its unique identifier.

```java
import io.pinecone.clients.Pinecone;
import io.pinecone.clients.AsyncIndex;
import org.openapitools.db_data.client.ApiException;
...

// Initialize pinecone object
Pinecone pinecone = new Pinecone.Builder("PINECONE_API_KEY").build();
// Get async imports connection object
AsyncIndex asyncIndex = pinecone.getAsyncIndexConnection("PINECONE_INDEX_NAME");

// Cancel import
asyncIndex.cancelImport("2");
```

## Examples

- The data and control plane operation examples can be found in `io/pinecone/integration` folder.