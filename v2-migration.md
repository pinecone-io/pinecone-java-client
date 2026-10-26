# Java SDK v2.0.0 Migration Guide

This migration guide is specific to migrating from versions "**v1.x**" to "**v2.0.x**". If you are on a newer version, see the [README](README.md) for the current API.

## Changes overview

- Added deletion protection feature which will impact the following existing methods:
    - `createServerlessIndex()` now requires a new argument: Enum `DeletionProtection`
    - Renamed `configureIndex()` to `configurePodsIndex()`; the overload that sets the pod type and replicas now also
      requires a `DeletionProtection` argument
- Moved the control plane model classes, such as `IndexModel`, from `org.openapitools.client.model` to
  `org.openapitools.control.client.model`. Update your imports.

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
import org.openapitools.control.client.model.DeletionProtection;
import org.openapitools.control.client.model.IndexModel;
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
import org.openapitools.control.client.model.DeletionProtection;
...

Pinecone pinecone = new Pinecone.Builder("PINECONE_API_KEY").build();

String indexName = "my-index";
String podType = "p1.x1";
int newNumberOfReplicas = 7;

pinecone.configurePodsIndex(indexName, podType, newNumberOfReplicas, DeletionProtection.DISABLED);
```