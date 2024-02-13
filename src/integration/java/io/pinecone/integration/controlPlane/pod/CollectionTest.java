package io.pinecone.integration.controlPlane.pod;

import io.pinecone.*;
import io.pinecone.helpers.RandomStringBuilder;
import io.pinecone.proto.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openapitools.client.model.*;

import java.util.Arrays;
import java.util.List;

import static io.pinecone.helpers.IndexManager.createNewIndexAndConnect;
import static io.pinecone.helpers.IndexManager.waitUntilIndexIsReady;
import static io.pinecone.helpers.IndexManager.createCollection;
import static io.pinecone.helpers.BuildUpsertRequest.*;
import static org.junit.jupiter.api.Assertions.*;

public class CollectionTest {

    private static PineconeControlPlaneClient controlPlaneClient;
    private static final String indexName = RandomStringBuilder.build("collection-test", 8);
    private static final IndexMetric indexMetric = IndexMetric.COSINE;
    private static final List<String> upsertIds = Arrays.asList("v1", "v2", "v3");
    private static final String namespace = RandomStringBuilder.build("ns", 8);
    private static final String apiKey = System.getenv("PINECONE_API_KEY");
    private static final String environment = System.getenv("PINECONE_ENVIRONMENT");
    private static final int dimension = 3;

    @BeforeAll
    public static void setUpIndex() throws InterruptedException {
        controlPlaneClient = new PineconeControlPlaneClient(apiKey);
        CreateIndexRequestSpecPod podSpec = new CreateIndexRequestSpecPod().pods(1).podType("p1.x1").replicas(1).environment(environment);
        CreateIndexRequestSpec spec = new CreateIndexRequestSpec().pod(podSpec);
        PineconeConnection dataPlaneConnection = createNewIndexAndConnect(controlPlaneClient, indexName, dimension, indexMetric, spec);
        VectorServiceGrpc.VectorServiceBlockingStub blockingStub = dataPlaneConnection.getBlockingStub();

        // Upsert vectors to index and sleep for freshness
        blockingStub.upsert(buildRequiredUpsertRequest(upsertIds, namespace));
        Thread.sleep(3500);
        dataPlaneConnection.close();
    }

    @AfterAll
    public static void deleteIndex() throws InterruptedException {
        Thread.sleep(5000);
        controlPlaneClient.deleteIndex(indexName);
    }

    @Test
    public void testIndexToCollectionHappyPath() throws InterruptedException {
        String collectionName = RandomStringBuilder.build("collection-test", 8);

        // Create collection from index
        CollectionModel collection = createCollection(controlPlaneClient, collectionName, indexName, true);

        assertEquals(collection.getName(), collectionName);
        assertEquals(collection.getEnvironment(), environment);
        assertEquals(collection.getStatus(), CollectionModel.StatusEnum.READY);

        // Verify collection is listed
        List<CollectionModel> collections = controlPlaneClient.listCollections().getCollections();
        boolean collectionFound = false;
        if (collections != null && !collections.isEmpty()) {
            for (CollectionModel col : collections) {
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
        String newIndexName = RandomStringBuilder.build("index-from-col-", 5);
        System.out.println("Creating index " + newIndexName + " from collection " + collectionName);

        CreateIndexRequestSpecPod podSpec = new CreateIndexRequestSpecPod().environment(environment).sourceCollection(collectionName);
        CreateIndexRequestSpec spec = new CreateIndexRequestSpec().pod(podSpec);
        CreateIndexRequest newCreateIndexRequest = new CreateIndexRequest().name(newIndexName).dimension(dimension).metric(indexMetric).spec(spec);
        IndexModel indexFromCollection = controlPlaneClient.createIndex(newCreateIndexRequest);
        System.out.println("Index " + newIndexName + " created from collection " + collectionName + ". Waiting until index is ready...");
        indexFromCollection = waitUntilIndexIsReady(controlPlaneClient, newIndexName);

        IndexModel indexDescription = controlPlaneClient.describeIndex(newIndexName);
        assertEquals(indexDescription.getName(), newIndexName);
        assertEquals(indexDescription.getSpec().getPod().getSourceCollection(), collectionName);
        assertEquals(indexDescription.getStatus().getReady(), true);

        // Set up new index data plane connection
        PineconeClient newIndexClient = new PineconeClient(new PineconeClientConfig().withApiKey(apiKey).withEnvironment(environment));
        PineconeConnection newIndexDataPlaneClient = newIndexClient.connect(new PineconeConnectionConfig().withConnectionUrl("https://" + indexFromCollection.getHost()));
        DescribeIndexStatsResponse describeResponse = newIndexDataPlaneClient.getBlockingStub().describeIndexStats(DescribeIndexStatsRequest.newBuilder().build());

        // Verify stats reflect the vectors in the collection
        assertEquals(describeResponse.getTotalVectorCount(), 3);

        // Verify the vectors from the collection -> new index can be fetched
        FetchResponse fetchedVectors = newIndexDataPlaneClient.getBlockingStub().fetch(FetchRequest.newBuilder().addAllIds(upsertIds).setNamespace(namespace).build());
        newIndexDataPlaneClient.close();

        for (String key : upsertIds) {
            assert (fetchedVectors.containsVectors(key));
        }

        // Verify we can delete the collection
        controlPlaneClient.deleteCollection(collectionName);
        Thread.sleep(2500);
        collections = controlPlaneClient.listCollections().getCollections();


        if (collections != null) {
            boolean isCollectionDeleted = true;
            for (CollectionModel col : collections) {
                if (col.getName().equals(collectionName)) {
                    isCollectionDeleted = false;
                    break;
                }
            }

            if (!isCollectionDeleted) {
                fail("Collection " + collectionName + " was not successfully deleted");
            }
        }

        // Clean up
        controlPlaneClient.deleteIndex(newIndexName);
    }

    @Test
    public void testIndexFromDifferentMetricCollection() throws InterruptedException {
        String collectionName = RandomStringBuilder.build("collection-test", 8);

        // Create collection from index
        CollectionModel collection = createCollection(controlPlaneClient, collectionName, indexName, true);

        assertEquals(collection.getName(), collectionName);
        assertEquals(collection.getEnvironment(), environment);
        assertEquals(collection.getStatus(), CollectionModel.StatusEnum.READY);

        // Use a different metric than the source index
        IndexMetric[] metrics = { IndexMetric.COSINE, IndexMetric.EUCLIDEAN, IndexMetric.DOTPRODUCT };
        IndexMetric targetMetric = IndexMetric.COSINE;
        for (IndexMetric metric : metrics) {
            if (!metric.equals(indexMetric)) {
                targetMetric = metric;
            }
        }

        String newIndexName = RandomStringBuilder.build("from-coll", 8);
        CreateIndexRequestSpecPod podSpec = new CreateIndexRequestSpecPod().environment(environment).sourceCollection(collectionName);
        CreateIndexRequestSpec spec = new CreateIndexRequestSpec().pod(podSpec);
        PineconeConnection dataPlaneConnection = createNewIndexAndConnect(controlPlaneClient, newIndexName, dimension, targetMetric, spec);

        IndexModel newIndex = controlPlaneClient.describeIndex(newIndexName);
        assertEquals(newIndex.getName(), newIndexName);
        assertEquals(newIndex.getMetric(), targetMetric);

        // Clean up
        controlPlaneClient.deleteIndex(newIndexName);
        controlPlaneClient.deleteCollection(collectionName);
        dataPlaneConnection.close();
    }

}
