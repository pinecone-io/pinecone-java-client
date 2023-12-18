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
  <version>0.7.0</version>
</dependency>
```

[comment]: <> (^ [pc:VERSION_LATEST_RELEASE])

Gradle:
```
implementation "io.pinecone:pinecone-client:0.7.0"
```

[comment]: <> (^ [pc:VERSION_LATEST_RELEASE])

Alternatively, you can use our standalone uberjar [pinecone-client-0.7.0-all.jar](https://repo1.maven.org/maven2/io/pinecone/pinecone-client/0.7.0/pinecone-client-0.7.0-all.jar), which bundles the pinecone client and all dependencies together inside a single jar. You can include this on your classpath like any 3rd party JAR without having to obtain the *pinecone-client* dependencies separately.

[comment]: <> (^ [pc:VERSION_LATEST_RELEASE])


## Examples

- The data and control plane operation examples can be found in `io/pinecone/integration` folder.