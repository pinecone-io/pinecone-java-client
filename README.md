# Pinecone Java Client

The Pinecone Java client lets JVM applications interact with Pinecone services.

## Requirements

*pinecone-client* requires at least Java 1.8.

## Installation

*pinecone-client* can be installed from [Maven Central](https://mvnrepository.com/artifact/io.pinecone/pinecone-client) for use as a maven dependency in the following ways:

Maven:
```
<dependency>
  <groupId>io.pinecone</groupId>
  <artifactId>pinecone-client</artifactId>
  <version>1.0.0</version>
</dependency>
```

[comment]: <> (^ [pc:VERSION_LATEST_RELEASE])

Gradle:
```
implementation "io.pinecone:pinecone-client:1.0.0"
```

[comment]: <> (^ [pc:VERSION_LATEST_RELEASE])

Alternatively, you can use our standalone uberjar [pinecone-client-1.0.0-all.jar](https://repo1.maven.org/maven2/io/pinecone/pinecone-client/1.0.0/pinecone-client-1.0.0-all.jar), which bundles the pinecone 
client and all dependencies together. You can include this in your classpath like you do with any 3rd party JAR without 
having to obtain the *pinecone-client* dependencies separately.

[comment]: <> (^ [pc:VERSION_LATEST_RELEASE])

## Usage

### Initializing the client

Before you can use the Pinecone Java SDK, you must sign up for a Pinecone account and find your API key in the Pinecone 
console 
dashboard at [https://app.pinecone.io](https://app.pinecone.io).

#### Using apiKey

The `Pinecone` class is your main entry point into the Pinecone Java SDK. You can instantiate the client with 
your `apiKey`, either by passing it as an argument in your code or by setting it as an environment variable called 
`PINECONE_API_KEY`.

Note: for pod-based indexes, you will also need an `environment` variable. You can set pass this as an argument in 
your code or set it as an environment variable called `PINECONE_ENVIRONMENT`.

```java
import io.pinecone.clients.Pinecone;
import org.openapitools.client.model.*;

public class InitializeClientExample {
    public static void main(String[] args) {
        Pinecone pinecone = new Pinecone.Builder("PINECONE_API_KEY").build();
    }
}
```

#### Passing OkHttpClient
```java
import io.pinecone.clients.Pinecone;

public class InitializeClientExample {
    public static void main(String[] args) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS);

        OkHttpClient httpClient = builder.build();

        Pinecone pinecone = new Pinecone.Builder("PINECONE_API_KEY").build();
    }
}
```

# Indexes
Operations related to the building and managing of Pinecone indexes are called [control plane](https://docs.pinecone.io/reference/api/introduction#control-plane) operations.

## Create Index
You can use the Java SDK to create two types of indexes: [serverless indexes](https://docs.pinecone.io/guides/indexes/understanding-indexes#serverless-indexes) (recommended for most use cases) and 
[pod-based indexes](https://docs.pinecone.io/guides/indexes/understanding-indexes#pod-based-indexes) (recommended for high-throughput use cases).

### Create a serverless index

> [!WARNING]  
> Serverless indexes are in **public preview** and are available only on AWS in the
> `us-east-1` and `us-west-2` regions. Check the [current limitations](https://docs.pinecone.io/docs/limits#serverless-index-limitations) and test thoroughly before using it in production.

```java
import io.pinecone.clients.Pinecone;
import org.openapitools.client.model.IndexModel;

public class CreateServerlessIndexExample {
    public static void main(String[] args) {
        Pinecone pinecone = new Pinecone.Builder("PINECONE_API_KEY").build();
        
        String indexName = "example-index";
        String similarityMetric = "cosine";
        int dimension = 1538;
        String cloud = "aws";
        String region = "us-west-2";

        IndexModel indexModel = pinecone.createServerlessIndex(indexName, similarityMetric, dimension, cloud, region);
    }
}
```

### Create a pod index
The following is a minimal example of creating a pod-based index. For all the possible configuration options, see 
`main/java/io/pinecone/clients/Pinecone.java`.

```java
import io.pinecone.clients.Pinecone;
import org.openapitools.client.model.IndexModel;
...
        
Pinecone pinecone = new Pinecone.Builder("PINECONE_API_KEY").build();
        
String indexName = "example-index";
String similarityMetric = "cosine"; // Optional; defaults to cosine similarity
int dimension = 1538;
String environment = "us-east-1-aws";
String podType = "p1.x1";

IndexModel indexModel = pinecone.createPodsIndex(indexName, dimension, environment, podType, similarityMetric);
```

## List indexes

The following example returns all indexes (and their corresponding metadata) in your project.

```java
import io.pinecone.clients.Pinecone;
import org.openapitools.client.model.IndexList;
...
        
Pinecone pinecone = new Pinecone.Builder("PINECONE_API_KEY").build();
IndexList indexesInYourProject = pinecone.listIndexes();
```

## Describe index

The following example returns metadata about an index.

```java
import io.pinecone.clients.Pinecone;
import org.openapitools.client.model.IndexModel;
...
        
Pinecone pinecone = new Pinecone.Builder("PINECONE_API_KEY").build(); 
IndexModel indexMetadata = pinecone.describeIndex("example-index");
```

## Delete an index

The following example deletes an index.

```java
import io.pinecone.clients.Pinecone;

Pinecone pinecone = new Pinecone.Builder("PINECONE_API_KEY").build();
pinecone.deleteIndex("example-index");
```

## Scale replicas

The following example changes the number of replicas for an index.

Note: scaling replicas is only applicable to pod-based indexes.

```java
import io.pinecone.clients.Pinecone;
import org.openapitools.client.model.IndexModel;
...
        
Pinecone pinecone = new Pinecone.Builder("PINECONE_API_KEY").build();

String indexName = "example-index";
String podType = "p1.x1";
int newNumberOfReplicas = 7;
        
pinecone.configureIndex(indexName, podType, newNumberOfReplicas);
```

## Describe index statistics

The following example returns statistics about an index.

```java
import io.pinecone.clients.Index;
import io.pinecone.clients.Pinecone;
import io.pinecone.proto.DescribeIndexStatsResponse;

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

## List vector IDs by Namespace
The following example lists up to 100 vector IDs from a particular namespace.

This method accepts optional parameters for `prefix` (list IDs filtered by a prefix), `limit` (retrieve `n` vector 
IDs), and `paginationToken` (retrieve the next set of vector IDs).

```java
import io.pinecone.clients.Index;
import io.pinecone.clients.Pinecone;
import io.pinecone.proto.ListResponse;

Pinecone pinecone = new Pinecone.Builder(System.getenv("PINECONE_API_KEY")).build();
String indexName = "example-index";
Index index = pinecone.getIndexConnection(indexName);
ListResponse listResponse = index.list("example-namespace");
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
Collections fall under data plane operations.

## Create collection

The following example creates the collection `example-collection` from
`example-index`.

```java
import io.pinecone.clients.Pinecone;
import org.openapitools.client.model.CollectionModel;
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
import org.openapitools.client.model.CollectionModel;
import java.util.List;
...

Pinecone pinecone = new Pinecone.Builder("PINECONE_API_KEY").build();
List<CollectionModel> collectionModels = pinecone.listCollections().getCollections();
```

## Describe a collection

The following example returns a description of the collection

```java
import io.pinecone.clients.Pinecone;
import org.openapitools.client.model.CollectionModel;
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

## Examples

- The data and control plane operation examples can be found in `io/pinecone/integration` folder.
- A full end-to-end Semantic Search example can be found in the [Java Examples](https://github.com/pinecone-io/java-examples/tree/main) repo on Github.