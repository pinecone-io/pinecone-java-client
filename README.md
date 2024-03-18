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
  <version>1.0.0-rc.1</version>
</dependency>
```

[comment]: <> (^ [pc:VERSION_LATEST_RELEASE])

Gradle:
```
implementation "io.pinecone:pinecone-client:1.0.0-rc.1"
```

[comment]: <> (^ [pc:VERSION_LATEST_RELEASE])

Alternatively, you can use our standalone uberjar [pinecone-client-1.0.0-rc.1-all.jar](https://repo1.maven.org/maven2/io/pinecone/pinecone-client/1.0.0-rc.1/pinecone-client-1.0.0-rc.1-all.jar), which bundles the pinecone client and all dependencies together inside a single jar. You can include this on your classpath like any 3rd party JAR without having to obtain the *pinecone-client* dependencies separately.

[comment]: <> (^ [pc:VERSION_LATEST_RELEASE])

## Usage

### Initializing the client

Before you can use the Pinecone SDK, you must sign up for an account and find your API key in the Pinecone console dashboard at [https://app.pinecone.io](https://app.pinecone.io).

#### Using apiKey

The `Pinecone` class is your main entry point into the Pinecone java SDK. You can instantiate the client with apiKey.

```java
import io.pinecone.clients.Pinecone;
import org.openapitools.client.model.*;

public class ControlPlaneExample {
    public static void main(String[] args) {
        Pinecone pinecone = new Pinecone("PINECONE_API_KEY");
    }
}
```

#### Passing OkHttpClient
```java
import io.pinecone.clients.Pinecone;

public class ControlPlaneExample {
    public static void main(String[] args) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS);

        OkHttpClient httpClient = builder.build();

        Pinecone pinecone = new Pinecone("PINECONE_API_KEY", httpClient);
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

public class ControlPlaneExample {
    public static void main(String[] args) {
        Pinecone pinecone = new Pinecone("PINECONE_API_KEY");
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

public class ControlPlaneExample {
    public static void main(String[] args) {
        Pinecone pinecone = new Pinecone("PINECONE_API_KEY");
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

public class ControlPlaneExample {
    public static void main(String[] args) {
        Pinecone pinecone = new Pinecone("PINECONE_API_KEY");

        IndexList indexList = pinecone.listIndexes();
    }
}
```

## Describe index

The following example returns information about the index `example-index`.

```java
package pineconeexamples.controlPlane.pod;

import io.pinecone.clients.Pinecone;
import org.openapitools.client.model.*;

public class ControlPlaneExample {
    public static void main(String[] args) {
        Pinecone pinecone = new Pinecone("PINECONE_API_KEY");
        IndexModel indexModel = pinecone.describeIndex("example-index");
    }
}
```

## Delete an index

The following example deletes the index named `example-index`.

```java
import io.pinecone.clients.Pinecone;

public class ControlPlaneExample {
    public static void main(String[] args) {
        Pinecone pinecone = new Pinecone("PINECONE_API_KEY");
        pinecone.deleteIndex("example-index");
    }
}
```

## Scale replicas

The following example changes the number of replicas for `example-index`.

```java
import io.pinecone.clients.Pinecone;
import org.openapitools.client.model.*;

public class ControlPlaneExample {
    public static void main(String[] args) {
        Pinecone pinecone = new Pinecone("PINECONE_API_KEY");
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
package pineconeexamples.controlPlane.pod;

import io.pinecone.clients.Index;
import io.pinecone.clients.Pinecone;
import io.pinecone.proto.DescribeIndexStatsResponse;

public class DataPlaneExample {
    public static void main(String[] args) {
        Pinecone pinecone = new Pinecone("PINECONE_API_KEY");

        Index index = pinecone.createIndexConnection("indexName");
        DescribeIndexStatsResponse indexStatsResponse = index.describeIndexStats(null);
    }
}
```

## Upsert vectors

The following example upserts vectors to `example-index`.

```java
import io.pinecone.clients.Index;
import io.pinecone.clients.Pinecone;
import io.pinecone.proto.UpsertResponse;

import java.util.Arrays;
import java.util.List;

public class DataPlaneExample {
    public static void main(String[] args) {
        Pinecone pinecone = new Pinecone("PINECONE_API_KEY");
        Index index = pinecone.createIndexConnection("example-index");
        List<Float> values = Arrays.asList(1.0f, 2.0f, 3.0f);
        UpsertResponse upsertResponse = index.upsert("v1", values, "namespace");
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

public class DataPlaneExample {
    public static void main(String[] args) {
        Pinecone pinecone = new Pinecone("PINECONE_API_KEY");
        Index index = pinecone.createIndexConnection("example-index");
        QueryResponseWithUnsignedIndices queryRespone = index.queryByVectorId(3, "v1", "namespace");
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

public class DataPlaneExample {
    public static void main(String[] args) {
        Pinecone pinecone = new Pinecone("PINECONE_API_KEY");
        Index index = pinecone.createIndexConnection("example-index");
        List<String> ids = Arrays.asList("v1", "v2", "v3");
        DeleteResponse deleteResponse = index.deleteByIds(ids);
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
        Pinecone pinecone = new Pinecone("PINECONE_API_KEY");
        Index index = pinecone.createIndexConnection("example-index");
        List<String> ids = Arrays.asList("v1", "v2", "v3");
        FetchResponse fetchResponse = index.fetch(ids);
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
        Pinecone pinecone = new Pinecone("PINECONE_API_KEY");
        Index index = pinecone.createIndexConnection("example-index");
        List<Float> values = Arrays.asList(1F, 2F, 3F);
        UpdateResponse updateResponse = index.update("v1", values, "namespace");
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
        Pinecone pinecone = new Pinecone("PINECONE_API_KEY");
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

public class Collections {
    public static void main(String[] args) {
        Pinecone pinecone = new Pinecone("PINECONE_API_KEY");
        List<CollectionModel> collectionList = pinecone.listCollections().getCollections();
    }
}
```

## Describe a collection

The following example returns a description of the collection

```java
import io.pinecone.clients.Pinecone;
import org.openapitools.client.model.CollectionModel;

public class Collections {
    public static void main(String[] args) {
        Pinecone pinecone = new Pinecone("PINECONE_API_KEY");
        CollectionModel collectionModel = pinecone.describeCollection("example-collection");
    }
}
```

## Delete a collection

The following example deletes the collection `example-collection`.

```java
import io.pinecone.clients.Pinecone;

public class Collections {
    public static void main(String[] args) {
        Pinecone pinecone = new Pinecone("PINECONE_API_KEY");
        pinecone.deleteCollection("example-collection");
    }
}
```

## Examples

- The data and control plane operation examples can be found in `io/pinecone/integration` folder.