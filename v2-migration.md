# Java SDK v2.0.0 Migration Guide

This migration guide is specific to migrating from versions "**v1.0.x**" and below to "**v2.0.x**".

## Changes overview

- Added deletion protection feature which will impact the following existing methods:
    - `createServerlessIndex()` is now accepted a new argument: Enum `DeletionProtection`
    - Renamed `configureIndex()` to `configurePodsIndex()`

## Indexes

### Creating a serverless index
Added enum `DeletionProtection` as an argument

**Before: ≤ 1.2.2**

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

IndexModel indexModel = pinecone.createServerlessIndex(indexName,
        similarityMetric,
        dimension,
        cloud,
        region);
```

**After: ≥ 2.0.0**

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

IndexModel indexModel = pinecone.createServerlessIndex(indexName, 
    similarityMetric, 
    dimension, 
    cloud, 
    region, 
    DeletionProtection.ENABLED);
```

## Configuring indexes
Renamed `configureIndex()` to `configurePodsIndex()`

**Before: ≤ 1.2.2**

```java
import io.pinecone.clients.Pinecone;
...

Pinecone pinecone = new Pinecone.Builder("PINECONE_API_KEY").build();

String indexName = "my-index";
String podType = "p1.x1";
int newNumberOfReplicas = 7;

pinecone.configureIndex(indexName, podType, newNumberOfReplicas);
```

**After: ≥ 2.0.0**

```java
import io.pinecone.clients.Pinecone;
...

Pinecone pinecone = new Pinecone.Builder("PINECONE_API_KEY").build();

String indexName = "my-index";
String podType = "p1.x1";
int newNumberOfReplicas = 7;

pinecone.configurePodsIndex(indexName, podType, newNumberOfReplicas);
```