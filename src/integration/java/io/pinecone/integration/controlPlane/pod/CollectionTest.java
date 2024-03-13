package io.pinecone.integration.controlPlane.pod;

import io.pinecone.clients.Pinecone;
import io.pinecone.configs.*;
import io.pinecone.helpers.RandomStringBuilder;
import io.pinecone.proto.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openapitools.client.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static io.pinecone.helpers.IndexManager.createNewIndexAndConnect;
import static io.pinecone.helpers.IndexManager.waitUntilIndexIsReady;
import static io.pinecone.helpers.IndexManager.createCollection;
import static io.pinecone.helpers.BuildUpsertRequest.*;
import static io.pinecone.helpers.AssertRetry.assertWithRetry;
import static org.junit.jupiter.api.Assertions.*;

public class CollectionTest {
    private static final String indexName = RandomStringBuilder.build("collection-test", 8);
    private static final String collectionName = RandomStringBuilder.build("collection-test", 8);
    private static final ArrayList<String> indexes = new ArrayList<>();
    private static final IndexMetric indexMetric = IndexMetric.COSINE;
    private static final List<String> upsertIds = Arrays.asList("v1", "v2", "v3");
    private static final String namespace = RandomStringBuilder.build("ns", 8);
    private static final String apiKey = System.getenv("PINECONE_API_KEY");
    private static final String environment = System.getenv("PINECONE_ENVIRONMENT");
    private static final int dimension = 4;
    private static final Logger logger = LoggerFactory.getLogger(CollectionTest.class);
    private static Pinecone controlPlaneClient;
    private static CollectionModel collection;

    @BeforeAll
    public static void setUp() throws InterruptedException {
        controlPlaneClient = new Pinecone(apiKey);

        // Create and upsert to index
        CreateIndexRequestSpecPod podSpec =
                new CreateIndexRequestSpecPod().pods(1).podType("p1.x1").replicas(1).environment(environment);
        CreateIndexRequestSpec spec = new CreateIndexRequestSpec().pod(podSpec);
        PineconeConnection dataPlaneConnection = createNewIndexAndConnect(controlPlaneClient, indexName, dimension,
                indexMetric, spec);
        VectorServiceGrpc.VectorServiceBlockingStub blockingStub = dataPlaneConnection.getBlockingStub();
        indexes.add(indexName);

        // Sometimes we see grpc failures when upserting so quickly after creating, so retry
        assertWithRetry(() -> blockingStub.upsert(buildRequiredUpsertRequestByDimension(upsertIds, dimension,
                namespace)), 1);
        dataPlaneConnection.close();

        // Create collection from index
        collection = createCollection(controlPlaneClient, collectionName, indexName, true);
        assertEquals(collection.getName(), collectionName);
        assertEquals(collection.getEnvironment(), environment);
        assertEquals(collection.getStatus(), CollectionModel.StatusEnum.READY);
    }

    @AfterAll
    public static void cleanUp() throws InterruptedException {
        // wait for things to settle before cleanup...
        Thread.sleep(2500);

        // Clean up indexes
        for (String index : indexes) {
            controlPlaneClient.deleteIndex(index);
        }

        // Verify we can delete the collection
        controlPlaneClient.deleteCollection(collectionName);
        Thread.sleep(2500);

        List<CollectionModel> collectionList = controlPlaneClient.listCollections().getCollections();
        if (collectionList != null) {
            boolean isCollectionDeleted = true;
            for (CollectionModel col : collectionList) {
                if (col.getName().equals(collectionName)) {
                    isCollectionDeleted = false;
                    break;
                }
            }

            if (!isCollectionDeleted) {
                fail("Collection " + collectionName + " was not successfully deleted");
            }
        }
    }

    @Test
    public void testIndexFromCollectionHappyPath() throws InterruptedException {
        // Verify collection is listed
        List<CollectionModel> collectionList = controlPlaneClient.listCollections().getCollections();
        boolean collectionFound = false;
        if (collectionList != null && !collectionList.isEmpty()) {
            for (CollectionModel col : collectionList) {
                if (col.getName().equals(collectionName)) {
                    collectionFound = true;
                    break;
                }
            }
        }

        if (!collectionFound) {
            fail("Collection " + collectionName + " was not found when listing collections");
        }

        // Verify collection can be described
        collection = controlPlaneClient.describeCollection(collectionName);

        assertEquals(collection.getStatus(), CollectionModel.StatusEnum.READY);
        assertEquals(collection.getDimension(), dimension);
        assertEquals(collection.getVectorCount(), 3);
        assertNotEquals(collection.getVectorCount(), null);
        assertTrue(collection.getSize() > 0);

        // Create index from collection
        String newIndexName = RandomStringBuilder.build("index-from-col", 5);
        logger.info("Creating index " + newIndexName + " from collection " + collectionName);

        CreateIndexRequestSpecPod podSpec =
                new CreateIndexRequestSpecPod().environment(environment).sourceCollection(collectionName);
        CreateIndexRequestSpec spec = new CreateIndexRequestSpec().pod(podSpec);
        CreateIndexRequest newCreateIndexRequest =
                new CreateIndexRequest().name(newIndexName).dimension(dimension).metric(indexMetric).spec(spec);
        controlPlaneClient.createIndex(newCreateIndexRequest);
        indexes.add(newIndexName);
        logger.info("Index " + newIndexName + " created from collection " + collectionName + ". Waiting until index " +
                "is ready...");
        waitUntilIndexIsReady(controlPlaneClient, newIndexName, 250000);

        assertWithRetry(() -> {
            IndexModel indexDescription = controlPlaneClient.describeIndex(newIndexName);
            assertEquals(indexDescription.getName(), newIndexName);
            assertEquals(indexDescription.getSpec().getPod().getSourceCollection(), collectionName);
            assertEquals(indexDescription.getStatus().getReady(), true);
        }, 3);

        // Set up new index data plane connection
        PineconeConfig config = new PineconeConfig(apiKey);
        PineconeConnection connection = new PineconeConnection(config, indexName);
        VectorServiceGrpc.VectorServiceBlockingStub newIndexBlockingStub = connection.getBlockingStub();

        assertWithRetry(() -> {
            DescribeIndexStatsResponse describeResponse = newIndexBlockingStub.describeIndexStats(DescribeIndexStatsRequest.newBuilder().build());

            // Verify stats reflect the vectors in the collection
            assertEquals(describeResponse.getTotalVectorCount(), 3);

            // Verify the vectors from the collection -> new index can be fetched
            FetchResponse fetchedVectors =
                    newIndexBlockingStub.fetch(FetchRequest.newBuilder().addAllIds(upsertIds).setNamespace(namespace).build());

            for (String key : upsertIds) {
                assert (fetchedVectors.containsVectors(key));
            }
        }, 1);

        connection.close();
    }

    @Test
    public void testIndexFromDifferentMetricCollection() throws InterruptedException {
        // Use a different metric than the source index
        IndexMetric[] metrics = {IndexMetric.COSINE, IndexMetric.EUCLIDEAN, IndexMetric.DOTPRODUCT};
        IndexMetric targetMetric = IndexMetric.COSINE;
        for (IndexMetric metric : metrics) {
            if (!metric.equals(indexMetric)) {
                targetMetric = metric;
            }
        }

        String newIndexName = RandomStringBuilder.build("from-coll", 8);
        CreateIndexRequestSpecPod podSpec =
                new CreateIndexRequestSpecPod().environment(environment).sourceCollection(collectionName);
        CreateIndexRequestSpec spec = new CreateIndexRequestSpec().pod(podSpec);
        PineconeConnection dataPlaneConnection = createNewIndexAndConnect(controlPlaneClient, newIndexName, dimension
                , targetMetric, spec);
        indexes.add(newIndexName);

        IndexModel newIndex = controlPlaneClient.describeIndex(newIndexName);
        assertEquals(newIndex.getName(), newIndexName);
        assertEquals(newIndex.getMetric(), targetMetric);

        dataPlaneConnection.close();
    }
}