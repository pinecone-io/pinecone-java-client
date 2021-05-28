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
  <version>0.1.3</version>
</dependency>
```

[comment]: <> (^ [pc:VERSION_LATEST_RELEASE])

Gradle:
```
implementation "io.pinecone:pinecone-client:0.1.3"
```

[comment]: <> (^ [pc:VERSION_LATEST_RELEASE])

Alternatively, you can use our standalone uberjar [pinecone-client-0.1.3-all.jar](https://repo1.maven.org/maven2/io/pinecone/pinecone-client/0.1.3/pinecone-client-0.1.3-all.jar), which bundles the pinecone client and all dependencies together inside a single jar. You can include this on your classpath like any 3rd party JAR without having to obtain the *pinecone-client* dependencies separately.

[comment]: <> (^ [pc:VERSION_LATEST_RELEASE])

## Features

The Java client doesn't support managing Pinecone services, only reading and writing from an existing service. To create or delete a service, use the Python client.

The read and write operations currently supported by the java client are `upsert` and `query`. Support for other operations is coming soon.

## Usage

Initialize the client:
```
PineconeClientConfig configuration = new PineconeClientConfig()
    .withApiKey("example-api-key");

PineconeClient pineconeClient = new PineconeClient(configuration);
```

Connect to a service:
```
PineconeConnectionConfig connectionConfig = new PineconeConnectionConfig()
    .withServiceAuthority("")
    .withServiceName("example-service");
    
PineconeConnection connection = pineconeClient.connect(connectionConfig);
```

A `PineconeConnection` manages underlying gRPC resources used for submitting operations to a particular service. The `PineconeConnection` can be shared and used concurrently by multiple threads.

Read and write data to the service:
```
// upsert vectors <1,2> and <3,4>
UpsertResponse upsertResponse = 
    connection.send(pineconeClient.upsertRequest()
        .ids(Arrays.asList("v1", "v2"))
        .data(new float[][]{{1F, 2F},{3F, 4F}}));

// query the 1 most similar vector to <1, 2>
QueryResponse queryResponse = connection.send(pineconeClient.queryRequest()
    .topK(1)
    .data(new float[][]{{1F, 2F}}));
```

When finished with a `PineconeConnection`, release its resources:
```
connection.close()
```

## Examples
See the *examples* directory for an example
application using *pinecone-client*.

[java-basic-mvn](examples/java-basic-mvn/src/main/java/pineconeexamples):
- The most basic example usage is in *MinimalUpsertAndQueryExample.java*
- A more involved example with multiple threads is in *UpsertsAndQueriesConcurrentExample.java*.
