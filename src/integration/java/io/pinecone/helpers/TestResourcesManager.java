package io.pinecone.helpers;

import io.pinecone.clients.AsyncIndex;
import io.pinecone.clients.Index;
import io.pinecone.clients.Pinecone;
import io.pinecone.exceptions.PineconeException;
import io.pinecone.proto.DescribeIndexStatsResponse;
import org.openapitools.db_control.client.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static io.pinecone.helpers.BuildUpsertRequest.buildRequiredUpsertRequestByDimension;
import static io.pinecone.helpers.TestUtilities.*;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * TestResourcesManager is a singleton class that manages the creation and cleanup of shared integration test
 * resources such as indexes and collections. It provides utility methods that allow tests to get or create shared resources
 * and configuration values for indexes and collections such as dimension, metric, and pre-seeded vectors.
 * <p>
 * The singleton is designed to only spin up index and collection resources as needed, so there's flexibility when running
 * a limited number of tests or files, and we don't need to wait for all indexes or collections to be ready. For cleanup,
 * {@code TestResourcesManager.getInstance().cleanupResources()} is called from {@link io.pinecone.CleanupAllTestResourcesListener}
 * which is run at the end of all junit tests.
 * <p>
 * To use the singleton in tests, call {@code TestResourcesManager.getInstance()} to get the instance of the singleton, then request
 * index and or collection resources as needed. Resources will be created if they don't exist, and shared if they've been created by a
 * previous test.
 * <p>
 * Example:
 * <pre>{@code
 *     import io.pinecone.helpers.TestResourcesManager;
 *     ...
 *     TestResourcesManager testResourcesManager = TestResourcesManager.getInstance();
 *     // get or create a shared serverless index
 *     IndexModel index = testResourcesManager.getOrCreateServerlessIndexModel();
 * }</pre>
 */
public class TestResourcesManager {
    private static final Logger logger = LoggerFactory.getLogger(TestUtilities.class);
    private static TestResourcesManager instance;
    private static final String apiKey = System.getenv("PINECONE_API_KEY");
    private final int dimension = System.getenv("DIMENSION") == null
            ? 4
            : Integer.parseInt(System.getenv("DIMENSION"));
    private final String environment = System.getenv("PINECONE_ENVIRONMENT") == null
            ? "us-east4-gcp"
            : System.getenv("PINECONE_ENVIRONMENT");
    private static final String metric = System.getenv("METRIC") == null
            ? IndexModel.MetricEnum.DOTPRODUCT.toString()
            : IndexModel.MetricEnum.valueOf(System.getenv("METRIC")).toString();
    private static final String cloud = System.getenv("CLOUD") == null
            ? ServerlessSpec.CloudEnum.AWS.toString()
            : System.getenv("CLOUD");
    private static final String region = System.getenv("REGION") == null
            ? "us-west-2"
            : System.getenv("REGION");
    private final Pinecone pineconeClient;
    private String podIndexName;
    private IndexModel podIndexModel;
    private IndexModel serverlessIndexModel;
    private String serverlessIndexName;
    private String collectionName;
    private CollectionModel collectionModel;
    private final List<String> vectorIdsForDefaultNamespace = Arrays.asList("def-id1", "def-id2", "def-prefix-id3", "def-prefix-id4");
    private final List<String> vectorIdsForCustomNamespace = Arrays.asList("cus-id1", "cus-id2", "cus-prefix-id3", "cus" +
            "-prefix-id4");
    private final String customNamespace = "example-namespace";
    private final String defaultNamespace = "";


    private TestResourcesManager() {
        pineconeClient = new Pinecone
                .Builder(apiKey)
                .withSourceTag("pinecone_test")
                .build();
    }

    /**
     * Gets the instance of the TestResourcesManager singleton.
     *
     * @return the {@link TestResourcesManager} instance.
     */
    public static TestResourcesManager getInstance() {
        if (instance == null) {
            instance = new TestResourcesManager();
        }
        return instance;
    }

    /**
     * Gets the dimension of the indexes created by the manager. Can be configured with the DIMENSION environment variable.
     * Defaults to 4.
     *
     * @return the dimension of the indexes created by the manager.
     */
    public int getDimension() {
        return dimension;
    }

    /**
     * Gets the metric of the indexes created by the manager. Can be configured with the METRIC environment variable.
     * Defaults to dotproduct to allow for testing of sparse vectors.
     *
     * @return the metric of the indexes created by the manager.
     */
    public String getMetric() {
        return metric;
    };

    /**
     * Gets the environment that pod indexes are created in. Can be configured with the PINECONE_ENVIRONMENT environment variable.
     * Defaults to us-east4-gcp.
     *
     * @return the environment of the pod index created by the manager.
     */
    public String getEnvironment() {

        return environment;
    }

    /**
     * Gets the region that serverless indexes are created in. Can be configured with the REGION environment variable.
     * Defaults to us-west-2.
     *
     * @return the region of the serverless index created by the manager.
     */
    public String getRegion() {
        return region;
    }

    /**
     * Gets the cloud that serverless indexes are created in. Can be configured with the CLOUD environment variable.
     * Defaults to aws.
     *
     * @return the cloud of the serverless index created by the manager.
     */
    public String getCloud() {
        return cloud;
    }

    /**
     * Gets the custom namespace used to seed indexes. Equals "example-namespace".
     *
     * @return the custom namespace.
     */
    public String getCustomNamespace() {
        return customNamespace;
    }

    /**
     * Gets the default namespace used to seed indexes. Equals "".
     *
     * @return the default namespace.
     */
    public String getDefaultNamespace() {
        return defaultNamespace;
    }

    /**
     * Gets the vector ids seeded into the default namespace "".
     *
     * @return a list of the vector ids seeded into the default namespace.
     */
    public List<String> getVectorIdsFromDefaultNamespace() {
        return vectorIdsForDefaultNamespace;
    }

    /**
     * Gets the vector ids seeded into the custom namespace "example-namespace".
     *
     * @return a list of the vector ids seeded into the custom namespace.
     */
    public List<String> getVectorIdsFromCustomNamespace() {
        return vectorIdsForCustomNamespace;
    }

    /**
     * Gets an index connection to the serverless index created by the manager.
     * Creates the index before connecting if it doesn't exist.
     *
     * @return a {@link Index} connection to the serverless index.
     */
    public  Index getOrCreateServerlessIndexConnection() throws InterruptedException {
        return getInstance().pineconeClient.getIndexConnection(getOrCreateServerlessIndex());
    }

    /**
     * Gets an asynchronous index connection to the serverless index created by the manager.
     * Creates the index before connecting if it doesn't exist.
     *
     * @return a {@link AsyncIndex} connection to the serverless index.
     */
    public AsyncIndex getOrCreateServerlessAsyncIndexConnection() throws InterruptedException {
        return getInstance().pineconeClient.getAsyncIndexConnection(getOrCreateServerlessIndex());
    }

    /**
     * Gets an index connection to the pod index created by the manager.
     * Creates the index before connecting if it doesn't exist.
     *
     * @return a {@link Index} connection to the pod index.
     */
    public  Index getOrCreatePodIndexConnection() throws InterruptedException {
        return getInstance().pineconeClient.getIndexConnection(getOrCreatePodIndex());
    }

    /**
     * Gets an asynchronous index connection to the pod index created by the manager.
     * Creates the index before connecting if it doesn't exist.
     *
     * @return a {@link AsyncIndex} connection to the pod index.
     */
    public AsyncIndex getOrCreatePodAsyncIndexConnection() throws InterruptedException {
        return getInstance().pineconeClient.getAsyncIndexConnection(getOrCreatePodIndex());
    }

    /**
     * Gets the pod index model by calling describeIndex.
     * Creates the index before calling describeIndex if it doesn't exist.
     *
     * @return the {@link IndexModel} of the pod index.
     */
    public IndexModel getOrCreatePodIndexModel() throws InterruptedException {
        podIndexModel = pineconeClient.describeIndex(getOrCreatePodIndex());
        return podIndexModel;
    }

    /**
     * Gets the serverless index model by calling describeIndex.
     * Creates the index before calling describeIndex if it doesn't exist.
     *
     * @return the {@link IndexModel} of the serverless index.
     */
    public IndexModel getOrCreateServerlessIndexModel() throws InterruptedException {
        serverlessIndexModel = pineconeClient.describeIndex(getOrCreateServerlessIndex());
        return serverlessIndexModel;
    }

    /**
     * Gets the collection model by calling describeCollection
     * Creates the index before calling describeIndex if it doesn't exist.
     *
     * @return the {@link CollectionModel} of the serverless index.
     */
    public CollectionModel getOrCreateCollectionModel() throws InterruptedException {
        collectionModel = pineconeClient.describeCollection(getOrCreateCollection());
        return collectionModel;
    }

    /**
     * Cleans up any resources that have been created by the manager. Called in {@link io.pinecone.CleanupAllTestResourcesListener}
     * after all tests have finished running.
     */
    public void cleanupResources() {
        if (podIndexName != null) {
            pineconeClient.deleteIndex(podIndexName);
        }

        if (serverlessIndexName != null) {
            pineconeClient.deleteIndex(serverlessIndexName);
        }

        if (collectionName != null) {
            pineconeClient.deleteCollection(collectionName);
        }
    }

    /**
     * Gets or creates a pod index. If the pod index has already been created, the method will return the existing index's
     * name. If the pod index has not been created, the method will create a new pod index with a randomized name. The values
     * that have been set for dimension, environment, podType, and metric will be used. The index will be initially
     * seeded with vectors in the default namespace.
     *
     * @return the name of the pod index.
     * @throws InterruptedException if the thread is interrupted while waiting for the index to be ready.
     * @throws PineconeException if the API encounters an error during index creation or if any of the arguments are invalid.
     */
    public String getOrCreatePodIndex() throws InterruptedException, PineconeException {
        if (podIndexName != null) {
            return podIndexName;
        }

        String indexName = RandomStringBuilder.build("pod-index", 8);

        podIndexModel = pineconeClient.createPodsIndex(indexName, dimension, environment, "p1.x1", metric);
        waitUntilIndexIsReady(pineconeClient, indexName);

        // Additional sleep after index marked as ready to avoid "no healthy upstream" error
        Thread.sleep(30000);
        
        // Seed default vector IDs into default namespace
        seedIndex(vectorIdsForDefaultNamespace, indexName, defaultNamespace);

        this.podIndexName = indexName;
        return indexName;
    }

    /**
     * Gets or creates a serverless index. If the serverless index has already been created, the method will return the existing index's
     * name. If the serverless index has not been created, the method will create a new serverless index with a randomized name. The values
     * that have been set for metric, dimension, cloud, and region will be used. The index will be initially seeded with vectors in
     * the default and custom namespaces.
     *
     * @return the name of the serverless index.
     * @throws InterruptedException if the thread is interrupted while waiting for the index to be ready.
     * @throws PineconeException if the API encounters an error during index creation or if any of the arguments are invalid.
     */
    public String getOrCreateServerlessIndex() throws InterruptedException, PineconeException {
        if (this.serverlessIndexName != null) {
            return this.serverlessIndexName;
        }

        String indexName = RandomStringBuilder.build("serverless-index", 8);
        HashMap<String, String> tags = new HashMap<>();
        tags.put("env", "testing");
        
        serverlessIndexModel = pineconeClient.createServerlessIndex(indexName, metric, dimension, cloud,
                region, DeletionProtection.DISABLED, tags);
        waitUntilIndexIsReady(pineconeClient, indexName);

        // Explicitly wait after ready to avoid the "no healthy upstream" issue
        Thread.sleep(30000);

        // Seed default vector IDs into default namespace, seed custom vector IDs into custom namespace; all in
        // same index
        seedIndex(vectorIdsForDefaultNamespace, indexName, defaultNamespace);
        seedIndex(vectorIdsForCustomNamespace, indexName, customNamespace);

        this.serverlessIndexName = indexName;
        return indexName;
    }

    /**
     * Gets or creates a collection. If the pod index has been created, it will be used to create the collection. If the pod
     * index has not been created, it will be created first. The collection will be created with a randomized name.
     *
     * @return the name of the collection.
     * @throws InterruptedException if the thread is interrupted while waiting for the collection to be ready.
     * @throws PineconeException if the API encounters an error during collection creation.
     */
    public String getOrCreateCollection() throws InterruptedException, PineconeException {
        if (collectionName != null) {
            return collectionName;
        }

        // Create index if not exists
        String sourceIndexName = getOrCreatePodIndex();

        // Create collection
        collectionName = RandomStringBuilder.build("collection", 8);
        collectionModel = pineconeClient.createCollection(collectionName, sourceIndexName);

        // Wait until collection is ready
        int timeWaited = 0;
        CollectionModel.StatusEnum collectionReady = collectionModel.getStatus();
        while (collectionReady != CollectionModel.StatusEnum.READY && timeWaited < 120000) {
            logger.info("Waiting for collection " + collectionName + " to be ready. Waited " + timeWaited + " " +
                    "milliseconds...");
            Thread.sleep(5000);
            timeWaited += 5000;
            collectionModel = pineconeClient.describeCollection(collectionName);
            collectionReady = collectionModel.getStatus();
        }

        if (timeWaited > 120000) {
            fail("Collection: " + collectionName + " is not ready after 120 seconds");
        }

        return collectionName;
    }

    private void seedIndex(List<String> vectorIds, String indexName, String namespace) throws InterruptedException {
        // Build upsert request
        Index indexClient = pineconeClient.getIndexConnection(indexName);
        indexClient.upsert(buildRequiredUpsertRequestByDimension(vectorIds, dimension), namespace);

        // Wait for record freshness
        DescribeIndexStatsResponse indexStats = indexClient.describeIndexStats();
        int totalTimeWaitedForVectors = 0;
        while (indexStats.getTotalVectorCount() == 0 || totalTimeWaitedForVectors <= 60000) {
            Thread.sleep(2000);
            totalTimeWaitedForVectors += 2000;
            indexStats = indexClient.describeIndexStats();
        }
        if (indexStats.getTotalVectorCount() == 0 && totalTimeWaitedForVectors >= 60000) {
            throw new PineconeException("Failed to seed index " + indexName + "with vectors");
        }
    }
}
