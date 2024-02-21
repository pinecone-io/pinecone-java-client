package io.pinecone.integration.controlPlane.pod;

import io.pinecone.PineconeConnection;
import io.pinecone.PineconeControlPlaneClient;
import io.pinecone.exceptions.PineconeException;
import io.pinecone.helpers.RandomStringBuilder;
import io.pinecone.proto.VectorServiceGrpc;
import org.openapitools.client.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import static io.pinecone.helpers.IndexManager.createNewIndexAndConnect;
import static io.pinecone.helpers.IndexManager.createCollection;
import static io.pinecone.helpers.IndexManager.waitUntilIndexIsReady;
import static io.pinecone.helpers.BuildUpsertRequest.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CollectionErrorTest {
    private static final String apiKey = System.getenv("PINECONE_API_KEY");
    private static final String environment = System.getenv("PINECONE_ENVIRONMENT");
    private static final String indexName = RandomStringBuilder.build("collection-error-test", 8);
    private static final String collectionName = RandomStringBuilder.build("reusable-coll", 8);
    private static final ArrayList<String> indexes = new ArrayList<>();
    private static final ArrayList<String> collections = new ArrayList<>();
    private static final List<String> upsertIds = Arrays.asList("v1", "v2", "v3");
    private static final int dimension = 4;
    private static PineconeControlPlaneClient controlPlaneClient;
    private static final Logger logger = LoggerFactory.getLogger(CollectionErrorTest.class);

    @BeforeAll
    public static void setUpIndexAndCollection() throws InterruptedException {
        controlPlaneClient = new PineconeControlPlaneClient(apiKey);
        CreateIndexRequestSpecPod podSpec = new CreateIndexRequestSpecPod().pods(1).podType("p1.x1").replicas(1).environment(environment);
        CreateIndexRequestSpec spec = new CreateIndexRequestSpec().pod(podSpec);
        PineconeConnection dataPlaneConnection = createNewIndexAndConnect(controlPlaneClient, indexName, dimension, IndexMetric.COSINE, spec);
        VectorServiceGrpc.VectorServiceBlockingStub blockingStub = dataPlaneConnection.getBlockingStub();
        indexes.add(indexName);

        // Upsert vectors to index and sleep for freshness
        blockingStub.upsert(buildRequiredUpsertRequestByDimension(upsertIds, dimension, ""));
        dataPlaneConnection.close();
        Thread.sleep(3500);

        // Create collection from index
        createCollection(controlPlaneClient, collectionName, indexName, true);
        collections.add(collectionName);
    }

    @AfterAll
    public static void cleanUp() throws InterruptedException {
        // wait for things to settle before cleanup...
        Thread.sleep(2500);
        for (String index : indexes) {
            controlPlaneClient.deleteIndex(index);
        }
        for (String collection : collections) {
            controlPlaneClient.deleteCollection(collection);
        }
        Thread.sleep(2500);
    }

    @Test
    public void testCreateCollectionFromInvalidIndex() {
        try {
            CreateCollectionRequest createCollectionRequest = new CreateCollectionRequest().name(RandomStringBuilder.build("coll1", 8)).source("invalid-index");
            controlPlaneClient.createCollection(createCollectionRequest);
        } catch (PineconeException exception) {
            logger.info("Exception: " + exception.getMessage());
            assertTrue(exception.getMessage().contains("Resource invalid-index not found"));
        }
    }
    @Test
    public void testIndexFromNonExistentCollection() {
        try {
            CreateIndexRequestSpecPod podSpec = new CreateIndexRequestSpecPod().environment(environment).sourceCollection("non-existent-collection");
            CreateIndexRequestSpec spec = new CreateIndexRequestSpec().pod(podSpec);
            CreateIndexRequest newCreateIndexRequest = new CreateIndexRequest().name(RandomStringBuilder.build("from-nonexistent-coll", 8)).dimension(dimension).metric(IndexMetric.COSINE).spec(spec);
            controlPlaneClient.createIndex(newCreateIndexRequest);
        } catch (PineconeException exception) {
            logger.info("Exception: " + exception.getMessage());
            assertTrue(exception.getMessage().contains("Resource non-existent-collection not found"));
        }
    }

    @Test
    public void testCreateIndexInMismatchedEnvironment() {
        try {
            List<String> environments = new LinkedList<>(Arrays.asList(
                    "eastus-azure",
                    "eu-west4-gcp",
                    "northamerica-northeast1-gcp",
                    "us-central1-gcp",
                    "us-west4-gcp",
                    "asia-southeast1-gcp",
                    "us-east-1-aws",
                    "asia-northeast1-gcp",
                    "eu-west1-gcp",
                    "us-east1-gcp",
                    "us-east4-gcp",
                    "us-west1-gcp"
            ));
            CollectionModel collection = controlPlaneClient.describeCollection(collectionName);
            environments.remove(collection.getEnvironment());
            String mismatchedEnv = environments.get(new Random().nextInt(environments.size()));

            CreateIndexRequestSpecPod podSpec = new CreateIndexRequestSpecPod().sourceCollection(collection.getName()).environment(mismatchedEnv);
            CreateIndexRequestSpec spec = new CreateIndexRequestSpec().pod(podSpec);
            CreateIndexRequest createIndexRequest = new CreateIndexRequest().name(RandomStringBuilder.build("from-coll", 8)).dimension(dimension).metric(IndexMetric.COSINE).spec(spec);
            controlPlaneClient.createIndex(createIndexRequest);
        } catch (PineconeException exception) {
            logger.info("Exception: " + exception.getMessage());
            assertTrue(exception.getMessage().contains("Source collection must be in the same environment as the index"));
        }
    }

    @Test
    @Disabled("Bug reported in #global-cps")
    public void testCreateIndexWithMismatchedDimension() {
        try {
            CollectionModel collection = controlPlaneClient.describeCollection(collectionName);
            CreateIndexRequestSpecPod podSpec = new CreateIndexRequestSpecPod().sourceCollection(collection.getName()).environment(collection.getEnvironment());
            CreateIndexRequestSpec spec = new CreateIndexRequestSpec().pod(podSpec);
            CreateIndexRequest createIndexRequest = new CreateIndexRequest().name(RandomStringBuilder.build("from-coll", 8)).dimension(dimension + 1).metric(IndexMetric.COSINE).spec(spec);
            controlPlaneClient.createIndex(createIndexRequest);
        } catch (PineconeException exception) {
            logger.info("Exception: " + exception.getMessage());
            assertTrue(exception.getMessage().contains("Index and collection must have the same dimension"));
        }
    }

    @Test
    public void testCreateCollectionFromNotReadyIndex() throws InterruptedException {
        String notReadyIndexName = RandomStringBuilder.build("from-coll4", 8);
        String newCollectionName = RandomStringBuilder.build("coll4-", 8);
        try {
            CreateIndexRequestSpecPod specPod = new CreateIndexRequestSpecPod().pods(1).podType("p1.x1").replicas(1).environment(environment);
            CreateIndexRequestSpec spec = new CreateIndexRequestSpec().pod(specPod);
            CreateIndexRequest createIndexRequest = new CreateIndexRequest().name(notReadyIndexName).dimension(dimension).metric(IndexMetric.COSINE).spec(spec);
            controlPlaneClient.createIndex(createIndexRequest);
            indexes.add(notReadyIndexName);

            createCollection(controlPlaneClient, newCollectionName, notReadyIndexName, true);
            collections.add(newCollectionName);
        } catch (PineconeException exception) {
            logger.info("Exception: " + exception.getMessage());
            assert (exception.getMessage().contains("Source index is not ready"));
        } finally {
            // Wait for index to initialize so it can be deleted in @AfterAll
            waitUntilIndexIsReady(controlPlaneClient, notReadyIndexName);
        }
    }
}