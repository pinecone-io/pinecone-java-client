package io.pinecone.integration.controlPlane.pod;

import io.pinecone.clients.Index;
import io.pinecone.clients.Pinecone;
import io.pinecone.exceptions.PineconeException;
import io.pinecone.exceptions.PineconeValidationException;
import io.pinecone.helpers.IndexManagerSingleton;
import io.pinecone.helpers.RandomStringBuilder;
import io.pinecone.proto.DescribeIndexStatsResponse;
import io.pinecone.proto.FetchResponse;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.openapitools.client.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static io.pinecone.helpers.AssertRetry.assertWithRetry;
import static io.pinecone.helpers.BuildUpsertRequest.buildRequiredUpsertRequestByDimension;
import static io.pinecone.helpers.IndexManager.*;
import static org.junit.jupiter.api.Assertions.*;

public class CollectionTest {
    private static final IndexManagerSingleton indexManager = IndexManagerSingleton.getInstance();
    private static Pinecone pineconeClient = indexManager.getPineconeClient();
    private static final Logger logger = LoggerFactory.getLogger(CollectionTest.class);
    private static final ArrayList<String> indexesToCleanUp = new ArrayList<>();
    private static final IndexMetric indexMetric = indexManager.getMetric();
    private static final List<String> upsertIds = indexManager.getPodIndexVectorIds();
    private static final String environment = indexManager.getEnvironment();
    private static final int dimension = indexManager.getDimension();
    private static CollectionModel collection;
    private static String indexName;
    private static String collectionName;

    @BeforeAll
    public static void setUp() throws InterruptedException {
        indexName = indexManager.getPodIndexName();
        collectionName = indexManager.getCollectionName();
        collection = indexManager.getCollectionModel();

//        CreateIndexRequestSpecPod podSpec =
//                new CreateIndexRequestSpecPod().pods(1).podType("p1.x1").replicas(1).environment(environment);
//        CreateIndexRequestSpec spec = new CreateIndexRequestSpec().pod(podSpec);
//        Index indexClient = createNewIndexAndConnectSync(pineconeClient, indexName, dimension,
//                indexMetric, spec);
//        indexesToCleanUp.add(indexName);
//
//        // Sometimes we see grpc failures when upserting so quickly after creating, so retry if so
//        assertWithRetry(() -> indexClient.upsert(buildRequiredUpsertRequestByDimension(upsertIds, dimension), namespace), 3);
//
//        // Create collection from index
//        collection = createCollection(pineconeClient, collectionName, indexName, true);
//        assertEquals(collection.getName(), collectionName);
//        assertEquals(collection.getEnvironment(), environment);
//        assertEquals(collection.getStatus(), CollectionModel.StatusEnum.READY);
    }

    @AfterAll
    public static void cleanUp() throws InterruptedException {
        // wait for things to settle before cleanup...
        Thread.sleep(2500);

//        // Verify we can delete the collection
//        pineconeClient.deleteCollection(collectionName);
//        Thread.sleep(2500);
//
//        List<CollectionModel> collectionList = pineconeClient.listCollections().getCollections();
//        if (collectionList != null) {
//            boolean isCollectionDeleted = true;
//            for (CollectionModel col : collectionList) {
//                if (col.getName().equals(collectionName)) {
//                    isCollectionDeleted = false;
//                    break;
//                }
//            }
//
//            if (!isCollectionDeleted) {
//                fail("Collection " + collectionName + " was not successfully deleted");
//            }
//        }

        // Clean up indexes
        for (String index : indexesToCleanUp) {
            pineconeClient.deleteIndex(index);
        }
    }

    @Test
    public void testIndexFromCollectionHappyPath() throws InterruptedException {
        // Verify collection is listed
        List<CollectionModel> collectionList = pineconeClient.listCollections().getCollections();
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
        collection = pineconeClient.describeCollection(collectionName);

        assertEquals(collection.getStatus(), CollectionModel.StatusEnum.READY);
        assertEquals(collection.getDimension(), dimension);
        assertEquals(collection.getVectorCount(), 3);
        assertNotEquals(collection.getVectorCount(), null);
        assertTrue(collection.getSize() > 0);

        // Create index from collection
        String newIndexName = RandomStringBuilder.build("from-coll", 5);
        logger.info("Creating index " + newIndexName + " from collection " + collectionName);

        CreateIndexRequestSpecPod podSpec =
                new CreateIndexRequestSpecPod().environment(environment).sourceCollection(collectionName);
        CreateIndexRequestSpec spec = new CreateIndexRequestSpec().pod(podSpec);
        CreateIndexRequest newCreateIndexRequest =
                new CreateIndexRequest().name(newIndexName).dimension(dimension).metric(indexMetric).spec(spec);
        pineconeClient.createIndex(newCreateIndexRequest);
        indexesToCleanUp.add(newIndexName);

        logger.info("Index " + newIndexName + " created from collection " + collectionName + ". Waiting until index is ready.");
        waitUntilIndexIsReady(pineconeClient, newIndexName, 120000);

        IndexModel indexDescription = pineconeClient.describeIndex(newIndexName);

        assertWithRetry(() -> {
            assertEquals(indexDescription.getName(), newIndexName);
            assertEquals(indexDescription.getSpec().getPod().getSourceCollection(), collectionName);
        }, 3);

        // If the index is ready, validate contents
        if (indexDescription.getStatus().getReady()) {
            // Set up new index data plane connection
            Index indexClient = pineconeClient.createIndexConnection(newIndexName);

            assertWithRetry(() -> {
                DescribeIndexStatsResponse describeResponse = indexClient.describeIndexStats();

                // Verify stats reflect the vectors in the collection
                assertEquals(describeResponse.getTotalVectorCount(), 3);

                // Verify the vectors from the collection -> new index can be fetched
                FetchResponse fetchedVectors = indexClient.fetch(upsertIds, "");
                for (String key : upsertIds) {
                    assertTrue(fetchedVectors.containsVectors(key));
                }
            });
        }
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
        createNewIndex(pineconeClient, newIndexName, dimension, targetMetric, spec, false);
        indexesToCleanUp.add(newIndexName);

        IndexModel newIndex = pineconeClient.describeIndex(newIndexName);
        assertEquals(newIndex.getName(), newIndexName);
        assertEquals(newIndex.getMetric(), targetMetric);
    }

    @Test
    public void testCreateCollectionFromNonExistentIndex() {
        try {
            pineconeClient.createCollection(RandomStringBuilder.build("coll1", 8), "nonexistentIndex");
            fail("Expected to throw PineconeException");
        } catch (PineconeException expected) {
            assertTrue(expected.getMessage().contains("nonexistentIndex not found"));
        }
    }

    @Test
    public void testCreateCollectionFromFromNullOrEmptyStringSourceIndex() {
        String collectionName = RandomStringBuilder.build("coll1", 8);
        // Empty string as sourceIndex
        try {
            pineconeClient.createCollection(collectionName, "");
            fail("Expected to throw PineconeValidationException");
        } catch (PineconeValidationException expected) {
            assertTrue(expected.getMessage().contains("sourceIndex cannot be null or empty"));
        }
        // Null as sourceIndex
        try {
            pineconeClient.createCollection(collectionName, null);
            fail("Expected to throw PineconeValidationException");
        } catch (PineconeValidationException expected) {
            assertTrue(expected.getMessage().contains("sourceIndex cannot be null or empty"));
        }
    }

    @Test
    public void testCreateCollectionFromNullOrEmptyStringCollectionName() {
        // Empty string as collectionName
        try {
            pineconeClient.createCollection("", indexName);
            fail("Expected to throw PineconeValidationException");
        } catch (PineconeValidationException expected) {
            assertTrue(expected.getMessage().contains("collectionName cannot be null or empty"));
        }
        // Null as collectionName
        try {
            pineconeClient.createCollection(null, indexName);
            fail("Expected to throw PineconeValidationException");
        } catch (PineconeValidationException expected) {
            assertTrue(expected.getMessage().contains("collectionName cannot be null or empty"));
        }
    }

    @Test
    public void testIndexFromNonExistentCollection() {
        try {
            CreateIndexRequestSpecPod podSpec = new CreateIndexRequestSpecPod().environment(environment).sourceCollection("non-existent-collection");
            CreateIndexRequestSpec spec = new CreateIndexRequestSpec().pod(podSpec);
            CreateIndexRequest newCreateIndexRequest = new CreateIndexRequest().name(RandomStringBuilder.build("from-nonexistent-coll", 8)).dimension(dimension).metric(IndexMetric.COSINE).spec(spec);
            pineconeClient.createIndex(newCreateIndexRequest);

            fail("Expected to throw PineconeException");
        } catch (PineconeException expected) {
            assertTrue(expected.getMessage().contains("non-existent-collection not found"));
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
            environments.remove(collection.getEnvironment());
            String mismatchedEnv = environments.get(new Random().nextInt(environments.size()));

            CreateIndexRequestSpecPod podSpec = new CreateIndexRequestSpecPod().sourceCollection(collection.getName()).environment(mismatchedEnv);
            CreateIndexRequestSpec spec = new CreateIndexRequestSpec().pod(podSpec);
            CreateIndexRequest createIndexRequest = new CreateIndexRequest().name(RandomStringBuilder.build("from-coll", 8)).dimension(dimension).metric(IndexMetric.COSINE).spec(spec);
            pineconeClient.createIndex(createIndexRequest);

            fail("Expected to throw PineconeException");
        } catch (PineconeException expected) {
            assertTrue(expected.getMessage().contains("collection must be in the same environment as the index"));
        }
    }

    @Test
    @Disabled("Bug reported in #global-cps")
    public void testCreateIndexWithMismatchedDimension() {
        try {
            CreateIndexRequestSpecPod podSpec = new CreateIndexRequestSpecPod().sourceCollection(collection.getName()).environment(collection.getEnvironment());
            CreateIndexRequestSpec spec = new CreateIndexRequestSpec().pod(podSpec);
            CreateIndexRequest createIndexRequest = new CreateIndexRequest().name(RandomStringBuilder.build("from-coll", 8)).dimension(dimension + 1).metric(IndexMetric.COSINE).spec(spec);
            pineconeClient.createIndex(createIndexRequest);

            fail("Expected to throw PineconeException");
        } catch (PineconeException expected) {
            assertTrue(expected.getMessage().contains("collection must have the same dimension"));
        }
    }

    @Test
    public void testCreateCollectionFromNotReadyIndex() throws InterruptedException {
        String notReadyIndexName = RandomStringBuilder.build("from-coll", 8);
        String newCollectionName = RandomStringBuilder.build("coll-", 8);
        try {
            CreateIndexRequestSpecPod specPod = new CreateIndexRequestSpecPod().pods(1).podType("p1.x1").replicas(1).environment(environment);
            CreateIndexRequestSpec spec = new CreateIndexRequestSpec().pod(specPod);
            CreateIndexRequest createIndexRequest = new CreateIndexRequest().name(notReadyIndexName).dimension(dimension).metric(IndexMetric.COSINE).spec(spec);
            pineconeClient.createIndex(createIndexRequest);
            indexesToCleanUp.add(notReadyIndexName);
            createCollection(pineconeClient, newCollectionName, notReadyIndexName, false);

            fail("Expected to throw PineconeException");
        } catch (PineconeException expected) {
            assertTrue(expected.getMessage().contains("index is not ready"));
        }
    }
}