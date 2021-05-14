# Pinecone Java Client

The Pinecone Java client lets JVM applications interact with Pinecone services.

## Requirements

*pinecone-client* requires at least Java 1.8.

## Installation

*pinecone-client* is not yet available in public package repositories, but you can find the latest release [here](https://github.com/pinecone-io/pinecone-java-client/releases). Download and unzip *pinecone-java-client-dist-${version}.zip*, then read on for how to integrate *pinecone-client* into your application.

### Dependency Managers
*pinecone-client* can be installed to your local maven repository for use as a maven dependency with the [maven-install-plugin](https://maven.apache.org/guides/mini/guide-3rd-party-jars-local.html):

```
mvn install:install-file \
 -Dfile=pinecone-client-${version}.jar \
 -Dsources=pinecone-client-${version}-sources.jar \
 -Djavadoc=pinecone-client-${version}-javadoc.jar \
 -DpomFile=pom-default.xml
```

Then you can add it as a dependency in the following ways:

Maven:
```
<dependency>
  <groupId>io.pinecone</groupId>
  <artifactId>pinecone-client</artifactId>
  <version>0.1.2</version>
</dependency>
```

Gradle:
```
implementation "io.pinecone:pinecone-client:0.1.2"
```

### Uberjar
*pinecone-client-${version}-all.jar* bundles the pinecone client and all dependencies together inside a single jar. You can include this on your classpath like any 3rd party JAR without having to obtain the *pinecone-client* dependencies separately. 

However, note that this may cause issues if your application already relies on other versions of these dependencies.

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
