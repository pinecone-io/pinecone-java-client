package io.pinecone.helpers;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.pinecone.clients.AsyncIndex;
import io.pinecone.clients.Index;
import io.pinecone.clients.Pinecone;
import io.pinecone.exceptions.PineconeException;
import io.pinecone.proto.DescribeIndexStatsResponse;
import io.pinecone.proto.NamespaceSummary;
import org.openapitools.db_control.client.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import static io.pinecone.helpers.BuildUpsertRequest.buildRequiredUpsertRequestByDimension;
import static io.pinecone.helpers.TestUtilities.*;

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
    private static final Logger logger = LoggerFactory.getLogger(TestResourcesManager.class);
    private static volatile TestResourcesManager instance;
    private static final String apiKey = System.getenv("PINECONE_API_KEY");
    private final int dimension = Integer.parseInt(envOrDefault("DIMENSION", "4"));
    private final String environment = envOrDefault("PINECONE_ENVIRONMENT", "us-east4-gcp");
    private static final String metric = envOrDefault("METRIC", "dotproduct");
    private static final String cloud = envOrDefault("CLOUD", "aws");
    private static final String region = envOrDefault("REGION", "us-west-2");
    private final Pinecone pineconeClient;
    // One lock per shared resource, so pod and serverless fixtures are created concurrently when test classes run in
    // parallel. collectionLock may take podLock (the collection is built from the pod index), never the reverse.
    private final Object podLock = new Object();
    private final Object serverlessLock = new Object();
    private final Object collectionLock = new Object();
    private String podIndexName;
    private IndexModel podIndexModel;
    private IndexModel serverlessIndexModel;
    private String serverlessIndexName;
    private String collectionName;
    private CollectionModel collectionModel;
    private Index serverlessIndexConnection;
    private AsyncIndex serverlessAsyncIndexConnection;
    private Index podIndexConnection;
    private AsyncIndex podAsyncIndexConnection;
    private final List<String> vectorIdsForDefaultNamespace = Arrays.asList("def-id1", "def-id2", "def-prefix-id3", "def-prefix-id4");
    private final List<String> vectorIdsForCustomNamespace = Arrays.asList("cus-id1", "cus-id2", "cus-prefix-id3", "cus" +
            "-prefix-id4");
    private final String customNamespace = "example-namespace";
    private final String defaultNamespace = "";


    // CI passes unset secrets through as empty strings, so treat blank the same as unset.
    private static String envOrDefault(String name, String defaultValue) {
        String value = System.getenv(name);
        return value == null || value.trim().isEmpty() ? defaultValue : value;
    }

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
    public static synchronized TestResourcesManager getInstance() {
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
    /**
     * Gets the host for the shared serverless index. Creates the index first if it doesn't exist.
     * Used by tests that need to construct their own PineconeConnection (e.g. to attach a custom listener)
     * without going through the shared static connection cache.
     *
     * @return the host URL of the shared serverless index.
     */
    public String getOrCreateServerlessIndexHost() throws InterruptedException {
        return pineconeClient.describeIndex(getOrCreateServerlessIndex()).getHost();
    }

    public Index getOrCreateServerlessIndexConnection() throws InterruptedException {
        synchronized (serverlessLock) {
            if (serverlessIndexConnection == null) {
                serverlessIndexConnection = pineconeClient.getIndexConnection(getOrCreateServerlessIndex());
            }
            return serverlessIndexConnection;
        }
    }

    /**
     * Gets an asynchronous index connection to the serverless index created by the manager.
     * Creates the index before connecting if it doesn't exist.
     *
     * @return a {@link AsyncIndex} connection to the serverless index.
     */
    public AsyncIndex getOrCreateServerlessAsyncIndexConnection() throws InterruptedException {
        synchronized (serverlessLock) {
            if (serverlessAsyncIndexConnection == null) {
                serverlessAsyncIndexConnection = pineconeClient.getAsyncIndexConnection(getOrCreateServerlessIndex());
            }
            return serverlessAsyncIndexConnection;
        }
    }

    /**
     * Gets an index connection to the pod index created by the manager.
     * Creates the index before connecting if it doesn't exist.
     *
     * @return a {@link Index} connection to the pod index.
     */
    public Index getOrCreatePodIndexConnection() throws InterruptedException {
        synchronized (podLock) {
            if (podIndexConnection == null) {
                podIndexConnection = pineconeClient.getIndexConnection(getOrCreatePodIndex());
            }
            return podIndexConnection;
        }
    }

    /**
     * Gets an asynchronous index connection to the pod index created by the manager.
     * Creates the index before connecting if it doesn't exist.
     *
     * @return a {@link AsyncIndex} connection to the pod index.
     */
    public AsyncIndex getOrCreatePodAsyncIndexConnection() throws InterruptedException {
        synchronized (podLock) {
            if (podAsyncIndexConnection == null) {
                podAsyncIndexConnection = pineconeClient.getAsyncIndexConnection(getOrCreatePodIndex());
            }
            return podAsyncIndexConnection;
        }
    }

    /**
     * Gets the pod index model by calling describeIndex.
     * Creates the index before calling describeIndex if it doesn't exist.
     *
     * @return the {@link IndexModel} of the pod index.
     */
    public IndexModel getOrCreatePodIndexModel() throws InterruptedException {
        synchronized (podLock) {
            podIndexModel = pineconeClient.describeIndex(getOrCreatePodIndex());
            return podIndexModel;
        }
    }

    /**
     * Gets the serverless index model by calling describeIndex.
     * Creates the index before calling describeIndex if it doesn't exist.
     *
     * @return the {@link IndexModel} of the serverless index.
     */
    public IndexModel getOrCreateServerlessIndexModel() throws InterruptedException {
        synchronized (serverlessLock) {
            serverlessIndexModel = pineconeClient.describeIndex(getOrCreateServerlessIndex());
            return serverlessIndexModel;
        }
    }

    /**
     * Gets the collection model by calling describeCollection
     * Creates the index before calling describeIndex if it doesn't exist.
     *
     * @return the {@link CollectionModel} of the serverless index.
     */
    public CollectionModel getOrCreateCollectionModel() throws InterruptedException {
        synchronized (collectionLock) {
            collectionModel = pineconeClient.describeCollection(getOrCreateCollection());
            return collectionModel;
        }
    }

    /**
     * Cleans up any resources that have been created by the manager. Called in {@link io.pinecone.CleanupAllTestResourcesListener}
     * after all tests have finished running. Steps run concurrently and every step runs even if another fails; each failure is
     * logged by name.
     */
    public void cleanupResources() {
        LinkedHashMap<String, ResourceCleanup.Step> steps = new LinkedHashMap<>();
        if (serverlessIndexConnection != null) {
            steps.put("close serverless index connection", serverlessIndexConnection::close);
        }
        if (serverlessAsyncIndexConnection != null) {
            steps.put("close serverless async index connection", serverlessAsyncIndexConnection::close);
        }
        if (podIndexConnection != null) {
            steps.put("close pod index connection", podIndexConnection::close);
        }
        if (podAsyncIndexConnection != null) {
            steps.put("close pod async index connection", podAsyncIndexConnection::close);
        }
        if (collectionName != null) {
            steps.put("delete collection " + collectionName,
                    () -> ResourceCleanup.deleteCollection(pineconeClient, collectionName));
        }
        if (podIndexName != null) {
            steps.put("delete index " + podIndexName, () -> ResourceCleanup.deleteIndex(pineconeClient, podIndexName));
        }
        if (serverlessIndexName != null) {
            steps.put("delete index " + serverlessIndexName,
                    () -> ResourceCleanup.deleteIndex(pineconeClient, serverlessIndexName));
        }
        ResourceCleanup.runAll(steps);
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
        synchronized (podLock) {
            if (podIndexName != null) {
                return podIndexName;
            }

            String indexName = RandomStringBuilder.buildResourceName("pod-index", 8);

            PodSpecMetadataConfig metadataConfig = null;
            podIndexModel = pineconeClient.createPodsIndex(indexName, dimension, environment, "p1.x1", metric, metadataConfig);
            logger.info("Created pod index " + indexName);
            waitUntilIndexIsReady(pineconeClient, indexName);

            // Seed default vector IDs into default namespace
            seedIndex(vectorIdsForDefaultNamespace, indexName, defaultNamespace);

            this.podIndexName = indexName;
            return indexName;
        }
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
        synchronized (serverlessLock) {
            if (this.serverlessIndexName != null) {
                return this.serverlessIndexName;
            }

            String indexName = RandomStringBuilder.buildResourceName("serverless-index", 8);
            HashMap<String, String> tags = new HashMap<>();
            tags.put("env", "testing");
        
            serverlessIndexModel = pineconeClient.createServerlessIndex(indexName, metric, dimension, cloud,
                    region, "disabled", tags);
            logger.info("Created serverless index " + indexName);
            waitUntilIndexIsReady(pineconeClient, indexName);

            // Seed default vector IDs into default namespace, seed custom vector IDs into custom namespace; all in
            // same index
            seedIndex(vectorIdsForDefaultNamespace, indexName, defaultNamespace);
            seedIndex(vectorIdsForCustomNamespace, indexName, customNamespace);

            this.serverlessIndexName = indexName;
            return indexName;
        }
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
        synchronized (collectionLock) {
            if (collectionName != null) {
                return collectionName;
            }

            // Create index if not exists
            String sourceIndexName = getOrCreatePodIndex();

            // Create collection
            collectionName = RandomStringBuilder.buildResourceName("collection", 8);
            collectionModel = pineconeClient.createCollection(collectionName, sourceIndexName);
            logger.info("Created collection " + collectionName + " from index " + sourceIndexName);

            collectionModel = waitUntilCollectionIsReady(pineconeClient, collectionName);

            return collectionName;
        }
    }

    private void seedIndex(List<String> vectorIds, String indexName, String namespace) throws InterruptedException {
        Index indexClient = pineconeClient.getIndexConnection(indexName);

        // The data plane can return UNAVAILABLE ("no healthy upstream") for a short time after the index reports
        // Ready, so retry the first write instead of sleeping before it.
        Poll.until("seed upsert into index " + indexName + " is accepted", 120000, 2000,
                () -> indexClient.upsert(buildRequiredUpsertRequestByDimension(vectorIds, dimension), namespace),
                response -> true,
                e -> e instanceof StatusRuntimeException
                        && ((StatusRuntimeException) e).getStatus().getCode() == Status.Code.UNAVAILABLE);

        Poll.until(vectorIds.size() + " seeded vectors are visible in namespace '" + namespace + "' of index "
                        + indexName, 120000, 2000,
                () -> namespaceVectorCount(indexClient.describeIndexStats(), namespace),
                count -> count >= vectorIds.size());
    }

    private static int namespaceVectorCount(DescribeIndexStatsResponse stats, String namespace) {
        NamespaceSummary summary = stats.getNamespacesMap().get(namespace);
        if (summary == null && namespace.isEmpty()) {
            summary = stats.getNamespacesMap().get("__default__");
        }
        return summary == null ? 0 : summary.getVectorCount();
    }
}
