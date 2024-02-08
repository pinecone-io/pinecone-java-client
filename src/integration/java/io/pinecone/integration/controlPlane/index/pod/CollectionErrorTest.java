package io.pinecone.integration.controlPlane.index.pod;

import io.pinecone.PineconeConnection;
import io.pinecone.PineconeControlPlaneClient;
import io.pinecone.exceptions.PineconeException;
import io.pinecone.helpers.RandomStringBuilder;
import io.pinecone.proto.VectorServiceGrpc;
import net.bytebuddy.utility.RandomString;
import org.junit.Ignore;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Disabled;
import org.openapitools.client.model.*;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static io.pinecone.helpers.IndexManager.createNewIndexAndConnect;
import static io.pinecone.helpers.IndexManager.createCollection;
import static io.pinecone.helpers.BuildUpsertRequest.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CollectionErrorTest {
    private static final String apiKey = System.getenv("PINECONE_API_KEY");
    private static final String environment = System.getenv("PINECONE_ENVIRONMENT");
    private static final String indexName = RandomStringBuilder.build("collection-error-test-", 8);
    private static final List<String> upsertIds = Arrays.asList("v1", "v2", "v3");
    private static final int dimension = 3;
    private static final String collectionName = RandomStringBuilder.build("reusable-coll-", 8);
    private static CollectionModel collection;
    private static PineconeControlPlaneClient controlPlaneClient;

    @BeforeAll
    public static void setUpIndexAndCollection() throws InterruptedException {
        controlPlaneClient = new PineconeControlPlaneClient(apiKey);
        CreateIndexRequestSpecPod podSpec = new CreateIndexRequestSpecPod().pods(1).podType("p1.x1").replicas(1).environment(environment);
        CreateIndexRequestSpec spec = new CreateIndexRequestSpec().pod(podSpec);
        PineconeConnection dataPlaneConnection = createNewIndexAndConnect(controlPlaneClient, indexName, dimension, IndexMetric.COSINE, spec);
        VectorServiceGrpc.VectorServiceBlockingStub blockingStub = dataPlaneConnection.getBlockingStub();

        // Upsert vectors to index and sleep for freshness
        blockingStub.upsert(buildRequiredUpsertRequest(upsertIds, ""));
        Thread.sleep(3500);
        dataPlaneConnection.close();

        // Create collection from index
        collection = createCollection(controlPlaneClient, collectionName, indexName, true);
    }

    @AfterAll
    public static void cleanUp() {
        controlPlaneClient.deleteIndex(indexName);
        controlPlaneClient.deleteCollection(collectionName);
    }

    @Test
    public void testCreateCollectionFromInvalidIndex() {
        try {
            CreateCollectionRequest createCollectionRequest = new CreateCollectionRequest().name(RandomStringBuilder.build("coll1-", 8)).source("invalid-index");
            controlPlaneClient.createCollection(createCollectionRequest);
        } catch (PineconeException exception) {
            assertTrue(exception.getMessage().contains("Resource invalid-index not found"));
        }
    }
    @Test
    public void testIndexFromNonExistentCollection() {
        try {
            CreateIndexRequestSpecPod podSpec = new CreateIndexRequestSpecPod().environment(environment).sourceCollection("non-existent-collection");
            CreateIndexRequestSpec spec = new CreateIndexRequestSpec().pod(podSpec);
            CreateIndexRequest newCreateIndexRequest = new CreateIndexRequest().name(RandomStringBuilder.build("from-nonexistent-coll-", 8)).dimension(3).metric(IndexMetric.COSINE).spec(spec);
            controlPlaneClient.createIndex(newCreateIndexRequest);
        } catch (PineconeException exception) {
            assertTrue(exception.getMessage().contains("Resource non-existent-collection not found"));
        }
    }

    @Test
    public void testCreateIndexInMismatchedEnvironment() {
        List<String> environments = Arrays.asList(
                "eastus-azure",
                "eu-west4-gcp",
                "northamerica-northeast1-gcp",
                "us-central1-gcp",
                "us-west4-gcp",
                "asia-southeast1-gcp",
                "us-east-1-aws",
                "asia-northeast1-gcp",
                "eu-west1-gcp",
                "eu-east1-gcp",
                "eu-east4-gcp",
                "us-west1-gcp"
        );
        environments.remove(collection.getEnvironment());
        String mismatchedEnv = environments.get(new Random().nextInt(environments.size()));

        try {
            CreateIndexRequestSpecPod podSpec = new CreateIndexRequestSpecPod().sourceCollection(collection.getName()).environment(mismatchedEnv);
            CreateIndexRequestSpec spec = new CreateIndexRequestSpec().pod(podSpec);
            CreateIndexRequest createIndexRequest = new CreateIndexRequest().name(RandomStringBuilder.build("from-coll-", 8)).dimension(dimension).metric(IndexMetric.COSINE).spec(spec);
            controlPlaneClient.createIndex(createIndexRequest);
        } catch (PineconeException exception) {
            assertTrue(exception.getMessage().contains("Source collection must be in the same environment as the index"));
        }
    }

    @Test
    @Disabled("Bug reported in #global-cps")
    public void testCreateIndexWithMismatchedDimension() {
        try {
            CreateIndexRequestSpecPod podSpec = new CreateIndexRequestSpecPod().sourceCollection(collection.getName()).environment(collection.getEnvironment());
            CreateIndexRequestSpec spec = new CreateIndexRequestSpec().pod(podSpec);
            CreateIndexRequest createIndexRequest = new CreateIndexRequest().name(RandomStringBuilder.build("from-coll-", 8)).dimension(dimension + 1).metric(IndexMetric.COSINE).spec(spec);
            controlPlaneClient.createIndex(createIndexRequest);
        } catch (PineconeException exception) {
            System.out.println("Exception: " + exception);
            assertTrue(exception.getMessage().contains("Index and collection must have the same dimension"));
        }
    }

    @Test
    public void testCreateCollectionFromNotReadyIndex() {
        try {
            String notReadyIndexName = RandomStringBuilder.build("from-coll4", 8);
            CreateIndexRequestSpecPod specPod = new CreateIndexRequestSpecPod().pods(1).podType("p1.x1").replicas(1).environment(environment);
            CreateIndexRequestSpec spec = new CreateIndexRequestSpec().pod(specPod);
            CreateIndexRequest createIndexRequest = new CreateIndexRequest().name(notReadyIndexName).dimension(dimension).metric(IndexMetric.COSINE).spec(spec);
            controlPlaneClient.createIndex(createIndexRequest);

            CreateCollectionRequest createCollectionRequest = new CreateCollectionRequest().name(RandomStringBuilder.build("coll4-", 8)).source(notReadyIndexName);
            controlPlaneClient.createCollection(createCollectionRequest);
        } catch (PineconeException exception) {
            System.out.println("EXCEPTION " + exception.getMessage());
            assertTrue(exception.getMessage().contains("Source index is not ready"));
        }
    }

}
