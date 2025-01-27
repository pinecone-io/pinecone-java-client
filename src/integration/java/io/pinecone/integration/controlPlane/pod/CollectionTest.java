package io.pinecone.integration.controlPlane.pod;

import io.pinecone.clients.Index;
import io.pinecone.clients.Pinecone;
import io.pinecone.exceptions.PineconeException;
import io.pinecone.exceptions.PineconeValidationException;
import io.pinecone.helpers.RandomStringBuilder;
import io.pinecone.helpers.TestResourcesManager;

import io.pinecone.proto.FetchResponse;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.openapitools.db_control.client.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static io.pinecone.helpers.AssertRetry.assertWithRetry;
import static io.pinecone.helpers.TestUtilities.*;
import static org.junit.jupiter.api.Assertions.*;

public class CollectionTest {
    private static final TestResourcesManager indexManager = TestResourcesManager.getInstance();
    private static final Pinecone pineconeClient = new Pinecone
            .Builder(System.getenv("PINECONE_API_KEY"))
            .withSourceTag("pinecone_test")
            .build();
    private static final Logger logger = LoggerFactory.getLogger(CollectionTest.class);
    private static final ArrayList<String> indexesToCleanUp = new ArrayList<>();
    private static final String sourceIndexMetric = indexManager.getMetric();
    private static final List<String> upsertIds = indexManager.getVectorIdsFromDefaultNamespace();
    private static final String environment = indexManager.getEnvironment();
    private static final int dimension = indexManager.getDimension();
    private static CollectionModel collection;
    private static String indexName;
    private static String collectionName;
    private static String namespace;

    @BeforeAll
    public static void setUp() throws InterruptedException {
        indexName = indexManager.getOrCreatePodIndex();
        collectionName = indexManager.getOrCreateCollection();
        collection = indexManager.getOrCreateCollectionModel();
        namespace = indexManager.getDefaultNamespace();
    }

    @AfterAll
    public static void cleanUp() throws InterruptedException {
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
        assertNotEquals(collection.getVectorCount(), null);
        assertTrue(collection.getSize() > 0);

        // Create index from collection
        String newIndexName = RandomStringBuilder.build("from-coll", 5);
        logger.info("Creating index " + newIndexName + " from collection " + collectionName);

        pineconeClient.createPodsIndex(newIndexName, dimension, environment, "p1.x1", sourceIndexMetric, collectionName);
        indexesToCleanUp.add(newIndexName);

        logger.info("Index " + newIndexName + " created from collection " + collectionName + ". Waiting until index is ready.");
        waitUntilIndexIsReady(pineconeClient, newIndexName, 120000);

        IndexModel indexDescription = pineconeClient.describeIndex(newIndexName);
        assertEquals(indexDescription.getName(), newIndexName);
        assertEquals(indexDescription.getSpec().getPod().getSourceCollection(), collectionName);

        // Wait to try and avoid "no healthy upstream" before interacting with the new index
        Thread.sleep(30000);

        // If the index is ready, validate contents
        if (indexDescription.getStatus().getState() == IndexModelStatus.StateEnum.READY) {
            // Set up new index data plane connection
            Index indexClient = pineconeClient.getIndexConnection(newIndexName);

            assertWithRetry(() -> {
                // Verify the vectors from the collection -> new index can be fetched
                FetchResponse fetchedVectors = indexClient.fetch(upsertIds, namespace);
                for (String key : upsertIds) {
                    assertTrue(fetchedVectors.containsVectors(key));
                }
            });
        }
    }

    @Test
    public void testCreateIndexFromCollectionWithDiffMetric() throws InterruptedException {
        // Use a different metric than the source index
        String[] metrics = {
                IndexModel.MetricEnum.COSINE.toString(),
                IndexModel.MetricEnum.EUCLIDEAN.toString(),
                IndexModel.MetricEnum.DOTPRODUCT.toString()
        };
        String targetMetric = IndexModel.MetricEnum.COSINE.toString();
        for (String metric : metrics) {
            if (!metric.equals(sourceIndexMetric)) {
                targetMetric = metric;
            }
        }

        String newIndexName = RandomStringBuilder.build("from-coll-with-diff-metric", 5);
        pineconeClient.createPodsIndex(newIndexName, dimension, environment, "p1.x1", targetMetric, collectionName);
        indexesToCleanUp.add(newIndexName);
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
            pineconeClient.createPodsIndex(RandomStringBuilder.build("from-nonexistent-coll", 8), dimension, environment, "p1.x1", sourceIndexMetric, "non-existent-collection");
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

            pineconeClient.createPodsIndex(RandomStringBuilder.build("from-coll", 8), dimension, mismatchedEnv, "p1.x1", sourceIndexMetric, collectionName);

            fail("Expected to throw PineconeException");
        } catch (PineconeException expected) {
            assertTrue(expected.getMessage().contains("collection must be in the same environment as the index"));
        }
    }

    @Test
    @Disabled("Bug reported in #global-cps")
    public void testCreateIndexWithMismatchedDimension() {
        try {
            pineconeClient.createPodsIndex(RandomStringBuilder.build("from-coll", 8), dimension + 1, environment, "p1.x1", sourceIndexMetric, collectionName);

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
            pineconeClient.createPodsIndex(notReadyIndexName, dimension, environment, "p1.x1", sourceIndexMetric, collectionName);
            indexesToCleanUp.add(notReadyIndexName);
            createCollection(pineconeClient, newCollectionName, notReadyIndexName, false);

            fail("Expected to throw PineconeException");
        } catch (PineconeException expected) {
            assertTrue(expected.getMessage().contains("index is not ready"));
        }
    }
}