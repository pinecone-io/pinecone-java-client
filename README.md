# Pinecone Java Client

The Pinecone Java client lets JVM applications interact with Pinecone services.

## Requirements

*pinecone-client* requires at least Java 1.8.

## Installation

*pinecone-client* can be installed from Maven Central for use as a maven dependency in the following ways:

Maven:
```
<dependency>
  <groupId>io.pinecone</groupId>
  <artifactId>pinecone-client</artifactId>
  <version>1.0.0-rc.4</version>
</dependency>
```

[comment]: <> (^ [pc:VERSION_LATEST_RELEASE])

Gradle:
```
implementation "io.pinecone:pinecone-client:1.0.0-rc.4"
```

[comment]: <> (^ [pc:VERSION_LATEST_RELEASE])

Alternatively, you can use our standalone uberjar [pinecone-client-1.0.0-rc.4-all.jar](https://repo1.maven.org/maven2/io/pinecone/pinecone-client/1.0.0-rc.4/pinecone-client-1.0.0-rc.4-all.jar), which bundles the pinecone client and all dependencies together inside a single jar. You can include this on your classpath like any 3rd party JAR without having to obtain the *pinecone-client* dependencies separately.

[comment]: <> (^ [pc:VERSION_LATEST_RELEASE])

## Usage

### Initializing the client

Before you can use the Pinecone SDK, you must sign up for an account and find your API key in the Pinecone console dashboard at [https://app.pinecone.io](https://app.pinecone.io).

#### Using apiKey

The `Pinecone` class is your main entry point into the Pinecone java SDK. You can instantiate the client with apiKey.

```java
import io.pinecone.clients.Pinecone;
import org.openapitools.client.model.*;

public class IntializeClientExample {
    public static void main(String[] args) {
        Pinecone pinecone = new Pinecone.Builder("PINECONE_API_KEY").build();
    }
}
```

#### Passing OkHttpClient
```java
import io.pinecone.clients.Pinecone;

public class IntializeClientExample {
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

## Create Index

### Create a serverless index

> [!WARNING]  
> Serverless indexes are in **public preview** and are available only on AWS in the
> `us-west-2` region. Check the [current limitations](https://docs.pinecone.io/docs/limits#serverless-index-limitations) and test thoroughly before using it in production.

```java
import io.pinecone.clients.Pinecone;
import org.openapitools.client.model.*;

public class CreateServerlessIndexExample {
    public static void main(String[] args) {
        Pinecone pinecone = new Pinecone.Builder("PINECONE_API_KEY").build();
        ServerlessSpec serverlessSpec = new ServerlessSpec().cloud(ServerlessSpec.CloudEnum.AWS).region("us-west-2");
        CreateIndexRequestSpec createIndexRequestSpec = new CreateIndexRequestSpec().serverless(serverlessSpec);
        CreateIndexRequest createIndexRequest = new CreateIndexRequest()
                .name("example-index")
                .metric(IndexMetric.COSINE)
                .dimension(10)
                .spec(createIndexRequestSpec);
        pinecone.createIndex(createIndexRequest);
    }
}
```

### Create a pod index

The following example creates an index without a metadata
configuration. By default, Pinecone indexes all metadata.

```java
import io.pinecone.clients.Pinecone;
import org.openapitools.client.model.*;

public class CreatePodIndexExample {
    public static void main(String[] args) {
        Pinecone pinecone = new Pinecone.Builder("PINECONE_API_KEY").build();
        CreateIndexRequestSpecPod podSpec = new CreateIndexRequestSpecPod().environment("PINECONE_ENVIRONMENT").podType("p1.x1");
        CreateIndexRequestSpec createIndexRequestSpec = new CreateIndexRequestSpec().pod(podSpec);
        CreateIndexRequest createIndexRequest = new CreateIndexRequest()
                .name("example-index")
                .metric(IndexMetric.COSINE)
                .dimension(10)
                .spec(createIndexRequestSpec);
        pinecone.createIndex(createIndexRequest);
    }
}
```

## List indexes

The following example returns all indexes in your project.

```java
import io.pinecone.clients.Pinecone;
import org.openapitools.client.model.*;

public class ListIndexExample {
    public static void main(String[] args) {
        Pinecone pinecone = new Pinecone.Builder("PINECONE_API_KEY").build();
        IndexList indexList = pinecone.listIndexes();
        System.out.println(indexList);
    }
}
```

## Describe index

The following example returns information about the index `example-index`.

```java
import io.pinecone.clients.Pinecone;
import org.openapitools.client.model.*;

public class DescribeIndexExample {
    public static void main(String[] args) {
        Pinecone pinecone = new Pinecone.Builder("PINECONE_API_KEY").build();
        IndexModel indexModel = pinecone.describeIndex("example-index");
        System.out.println(indexModel);
    }
}
```

## Delete an index

The following example deletes the index named `example-index`.

```java
import io.pinecone.clients.Pinecone;

public class DeleteIndexExample {
    public static void main(String[] args) {
        Pinecone pinecone = new Pinecone.Builder("PINECONE_API_KEY").build();
        pinecone.deleteIndex("example-index");
    }
}
```

## Scale replicas

The following example changes the number of replicas for `example-index`.

```java
import io.pinecone.clients.Pinecone;
import org.openapitools.client.model.*;

public class ConfigureIndexExample {
    public static void main(String[] args) {
        Pinecone pinecone = new Pinecone.Builder("PINECONE_API_KEY").build();
        ConfigureIndexRequestSpecPod pod = new ConfigureIndexRequestSpecPod().replicas(4);
        ConfigureIndexRequestSpec spec = new ConfigureIndexRequestSpec().pod(pod);
        ConfigureIndexRequest configureIndexRequest = new ConfigureIndexRequest().spec(spec);
        pinecone.configureIndex("example-index", configureIndexRequest);
    }
}

```

## Describe index statistics

The following example returns statistics about the index `example-index`.

```java
import io.pinecone.clients.Index;
import io.pinecone.clients.Pinecone;
import io.pinecone.proto.DescribeIndexStatsResponse;

public class DescribeIndexStatsExample {
    public static void main(String[] args) {
        Pinecone pinecone = new Pinecone.Builder("PINECONE_API_KEY").build();

        Index index = pinecone.getIndexConnection("example-index");
        DescribeIndexStatsResponse indexStatsResponse = index.describeIndexStats();
        System.out.println(indexStatsResponse);
    }
}
```

## Upsert vectors

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

public class UpsertVectorsExample {
    public static void main(String[] args) {
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

        // Call upsert data
        for (int i=0; i<metadataStructList.size(); i++) {
            vectors.add(buildUpsertVectorWithUnsignedIndices(upsertIds.get(i), values.get(i), sparseIndices.get(i), sparseValues.get(i), metadataStructList.get(i)));
        }

        UpsertResponse upsertResponse = index.upsert(vectors, "example-namespace");
        System.out.println(upsertResponse);
    }
}
```

## Query an index

The following example queries the index `example-index` with metadata
filtering.

```java
import io.pinecone.clients.Index;
import io.pinecone.clients.Pinecone;
import io.pinecone.unsigned_indices_model.QueryResponseWithUnsignedIndices;

public class QueryVectorsExample {
    public static void main(String[] args) {
        Pinecone pinecone = new Pinecone.Builder("PINECONE_API_KEY").build();
        Index index = pinecone.getIndexConnection("example-index");
        QueryResponseWithUnsignedIndices queryRespone = index.queryByVectorId(3, "v1", "example-namespace");
        System.out.println(queryRespone);
    }
}
```

## Delete vectors

The following example deletes vectors by ID.

```java
import io.pinecone.clients.Index;
import io.pinecone.clients.Pinecone;
import java.util.Arrays;
import java.util.List;

public class DeleteVectorsExample {
    public static void main(String[] args) {
        Pinecone pinecone = new Pinecone.Builder("PINECONE_API_KEY").build();
        Index index = pinecone.getIndexConnection("example-index");
        List<String> ids = Arrays.asList("v1", "v2", "v3");
        DeleteResponse deleteResponse = index.deleteByIds(ids, "example-namespace");
    }
}
```

## Fetch vectors

The following example fetches vectors by ID.

```java
import io.pinecone.clients.Index;
import io.pinecone.clients.Pinecone;
import io.pinecone.proto.FetchResponse;

import java.util.Arrays;
import java.util.List;

public class DataPlaneExample {
    public static void main(String[] args) {
        Pinecone pinecone = new Pinecone.Builder("PINECONE_API_KEY").build();
        Index index = pinecone.getIndexConnection("example-index");
        List<String> ids = Arrays.asList("v1", "v2", "v3");
        FetchResponse fetchResponse = index.fetch(ids, "example-namespace");
        System.out.println(fetchResponse);
    }
}
```

## Update vectors

The following example updates vectors by ID.

```java
import io.pinecone.clients.Index;
import io.pinecone.clients.Pinecone;
import io.pinecone.proto.UpdateResponse;

import java.util.Arrays;
import java.util.List;

public class DataPlaneExample {
    public static void main(String[] args) {
        Pinecone pinecone = new Pinecone.Builder("PINECONE_API_KEY").build();
        Index index = pinecone.getIndexConnection("example-index");
        List<Float> values = Arrays.asList(1F, 2F, 3F);
        UpdateResponse updateResponse = index.update("v1", values, "example-namespace");
        System.out.println(updateResponse);
    }
}
```

# Collections

## Create collection

The following example creates the collection `example-collection` from
`example-index`.

```java
import io.pinecone.clients.Pinecone;
import org.openapitools.client.model.CollectionModel;
import org.openapitools.client.model.CreateCollectionRequest;

public class Collections {
    public static void main(String[] args) {
        Pinecone pinecone = new Pinecone.Builder("PINECONE_API_KEY").build();
        String indexName = "example-index";
        String collectionName = "example-collection";
        
        CreateCollectionRequest createCollectionRequest = new CreateCollectionRequest().name(collectionName).source(indexName);
        CollectionModel collection = pinecone.createCollection(createCollectionRequest);
    }
}
```

## List collections

The following example returns a list of the collections in the current project.

```java
import io.pinecone.clients.Pinecone;
import org.openapitools.client.model.CollectionModel;

public class ListCollectionsExample {
    public static void main(String[] args) {
        Pinecone pinecone = new Pinecone.Builder("PINECONE_API_KEY").build();
        List<CollectionModel> collectionList = pinecone.listCollections().getCollections();
    }
}
```

## Describe a collection

The following example returns a description of the collection

```java
import io.pinecone.clients.Pinecone;
import org.openapitools.client.model.CollectionModel;

public class DescribeCollectionExample {
    public static void main(String[] args) {
        Pinecone pinecone = new Pinecone.Builder("PINECONE_API_KEY").build();
        CollectionModel collectionModel = pinecone.describeCollection("example-collection");
    }
}
```

## Delete a collection

The following example deletes the collection `example-collection`.

```java
import io.pinecone.clients.Pinecone;

public class DeleteCollectionExample {
    public static void main(String[] args) {
        Pinecone pinecone = new Pinecone.Builder("PINECONE_API_KEY").build();
        pinecone.deleteCollection("example-collection");
    }
}
```

## Examples

- The data and control plane operation examples can be found in `io/pinecone/integration` folder.