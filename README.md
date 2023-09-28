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
  <version>0.2.3</version>
</dependency>
```

[comment]: <> (^ [pc:VERSION_LATEST_RELEASE])

Gradle:
```
implementation "io.pinecone:pinecone-client:0.2.3"
```

[comment]: <> (^ [pc:VERSION_LATEST_RELEASE])

Alternatively, you can use our standalone uberjar [pinecone-client-0.2.3-all.jar](https://repo1.maven.org/maven2/io/pinecone/pinecone-client/0.2.3/pinecone-client-0.2.3-all.jar), which bundles the pinecone client and all dependencies together inside a single jar. You can include this on your classpath like any 3rd party JAR without having to obtain the *pinecone-client* dependencies separately.

[comment]: <> (^ [pc:VERSION_LATEST_RELEASE])

## Features

The Java client doesn't support managing Pinecone services, only reading and writing from existing indices. To create or delete an index, use the Python client.


## Examples

- The most basic example usage is in `src/test/java/io/pinecone/PineconeClientLiveIntegTest.java`, covering most basic operations.
> [!NOTE]
> The java-basic-mvn example is outdated.