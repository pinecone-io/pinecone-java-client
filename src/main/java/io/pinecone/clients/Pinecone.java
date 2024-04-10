package io.pinecone.clients;

import io.pinecone.configs.PineconeConfig;
import io.pinecone.configs.PineconeConnection;
import io.pinecone.exceptions.FailedRequestInfo;
import io.pinecone.exceptions.HttpErrorMapper;
import io.pinecone.exceptions.PineconeException;
import io.pinecone.exceptions.PineconeValidationException;
import okhttp3.OkHttpClient;
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.api.ManageIndexesApi;
import org.openapitools.client.model.*;

import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The Pinecone class is the main entry point for interacting with Pinecone via the Java SDK.
 * It is used to create, delete, and manage your indexes and collections.
 * <p>
 * Instantiating the Pinecone class requires you to leverage the {@link Pinecone.Builder} class to pass
 * an API key:
 * <pre>
 *     import io.pinecone.clients.Pinecone;
 *     import org.openapitools.client.model.ListResponse;
 *
 *     Pinecone client = new Pinecone.Builder(System.getenv("PINECONE_API_KEY")).build();
 *
 *     // Use the client to interact with Pinecone
 *     ListResponse indexes = client.listIndexes();
 * </pre>
 */
public class Pinecone {

    private static final ConcurrentHashMap<String, PineconeConnection> connectionsMap = new ConcurrentHashMap<>();
    private final ManageIndexesApi manageIndexesApi;
    private final PineconeConfig config;

    Pinecone(PineconeConfig config, ManageIndexesApi manageIndexesApi) {
        this.config = config;
        this.manageIndexesApi = manageIndexesApi;
    }

    /**
     * Creates a new serverless index with the specified parameters.
     *
     * <pre>
     *     import io.pinecone.clients.Pinecone;
     *
     *     Pinecone client = new Pinecone.Builder(System.getenv("PINECONE_API_KEY")).build();
     *
     *     client.createServerlessIndex("my-index", "cosine", 1536, "aws", "us-west-2");
     * </pre>
     *
     * @param indexName the name of the index to be created.
     * @param metric the metric type for the index. Must be one of "cosine", "euclidean", or "dot_product".
     * @param dimension the number of dimensions for the index.
     * @param cloud the cloud provider for the index.
     * @param region the cloud region for the index.
     * @return {@link IndexModel} representing the created serverless index.
     * @throws PineconeException if the API encounters an error during index creation or if any of the arguments are invalid.
     */
    public IndexModel createServerlessIndex(String indexName, String metric, int dimension, String cloud,
                                            String region) throws PineconeException {
        if (indexName == null || indexName.isEmpty()) {
            throw new PineconeValidationException("Index name cannot be null or empty");
        }

        if (metric == null || metric.isEmpty()) {
            throw new PineconeValidationException("Metric cannot be null or empty. Must be one of " + Arrays.toString(IndexMetric.values()));
        }
        try {
            IndexMetric.fromValue(metric.toLowerCase());
        } catch (IllegalArgumentException e) {
            throw new PineconeValidationException("Metric cannot be null or empty. Must be one of " + Arrays.toString(IndexMetric.values()));
        }

        if (dimension < 1) {
            throw new PineconeValidationException("Dimension must be greater than 0. See limits for more info: https://docs.pinecone.io/reference/limits");
        }

        if (cloud == null || cloud.isEmpty()) {
            throw new PineconeValidationException("Cloud cannot be null or empty. Must be one of " + Arrays.toString(ServerlessSpec.CloudEnum.values()));
        }
        try {
            ServerlessSpec.CloudEnum.fromValue(cloud.toLowerCase());
        } catch (IllegalArgumentException e) {
            throw new PineconeValidationException("Cloud cannot be null or empty. Must be one of " + Arrays.toString(ServerlessSpec.CloudEnum.values()));
        }

        if (region == null || region.isEmpty()) {
            throw new PineconeValidationException("Region cannot be null or empty");
        }

        // Convert user string for "metric" arg into IndexMetric
        IndexMetric userMetric = IndexMetric.fromValue(metric.toLowerCase());

        // Convert user string for "cloud" arg into ServerlessSpec.CloudEnum
        ServerlessSpec.CloudEnum cloudProvider = ServerlessSpec.CloudEnum.fromValue(cloud.toLowerCase());

        ServerlessSpec serverlessSpec = new ServerlessSpec().cloud(cloudProvider).region(region);
        CreateIndexRequestSpec createServerlessIndexRequestSpec = new CreateIndexRequestSpec().serverless(serverlessSpec);

        IndexModel indexModel = null;

        try {
            indexModel = manageIndexesApi.createIndex(new CreateIndexRequest()
                    .name(indexName)
                    .metric(userMetric)
                    .dimension(dimension)
                    .spec(createServerlessIndexRequestSpec));
        } catch (ApiException apiException) {
            handleApiException(apiException);
        }
        return indexModel;
    }

    /**
     * Overload for creating a new pods index with environment and podType, the minimum required parameters.
     *
     * <pre>
     *     import io.pinecone.clients.Pinecone;
     *
     *     Pinecone client = new Pinecone.Builder(System.getenv("PINECONE_API_KEY")).build();
     *
     *     client.createPodsIndex("my-index", 1536, "us-east4-gcp", "p1.x2");
     * </pre>
     *
     * @param indexName the name of the index to be created.
     * @param dimension the number of dimensions for the index.
     * @param environment the cloud environment where you want the index to be hosted.
     * @param podType the type of pod to use. A string with one of s1, p1, or p2 appended with a "." and one of x1, x2, x4, or x8.
     * @return {@link IndexModel} representing the created serverless index.
     * @throws PineconeException if the API encounters an error during index creation or if any of the arguments are invalid.
     */
    public IndexModel createPodsIndex(String indexName, Integer dimension, String environment, String podType) {
        return createPodsIndex(indexName, dimension, environment, podType, null, null, null, null, null, null);
    }

    /**
     * Overload for creating a new pods index with environment, podType, and metric.
     *
     * <pre>
     *     import io.pinecone.clients.Pinecone;
     *
     *     Pinecone client = new Pinecone.Builder(System.getenv("PINECONE_API_KEY")).build();
     *
     *     client.createPodsIndex("my-index", 1536, "us-east4-gcp", "p1.x2", "cosine");
     * </pre>
     *
     * @param indexName the name of the index to be created.
     * @param dimension the number of dimensions for the index.
     * @param environment the cloud environment where you want the index to be hosted.
     * @param podType the type of pod to use. A string with one of s1, p1, or p2 appended with a "." and one of x1, x2, x4, or x8.
     * @param metric the metric type for the index. Must be one of "cosine", "euclidean", or "dot_product".
     * @return {@link IndexModel} representing the created serverless index.
     * @throws PineconeException if the API encounters an error during index creation or if any of the arguments are invalid.
     */
    public IndexModel createPodsIndex(String indexName, Integer dimension, String environment,
                                      String podType, String metric) {
        return createPodsIndex(indexName, dimension, environment, podType, metric, null, null, null, null, null);
    }

    /**
     * Overload for creating a new pods index with environment, podType, metric, and metadataConfig.
     *
     * <pre>
     *     import io.pinecone.clients.Pinecone;
     *     import org.openapitools.client.model.CreateIndexRequestSpecPodMetadataConfig;
     *
     *     Pinecone client = new Pinecone.Builder(System.getenv("PINECONE_API_KEY")).build();
     *
     *     CreateIndexRequestSpecPodMetadataConfig metadataConfig = new CreateIndexRequestSpecPodMetadataConfig().fields(Arrays.asList("genre", "year"));
     *     client.createPodsIndex("my-index", 1536, "us-east4-gcp", "p1.x2", "cosine", metadataConfig);
     * </pre>
     *
     * @param indexName the name of the index to be created.
     * @param dimension the number of dimensions for the index.
     * @param environment the cloud environment where you want the index to be hosted.
     * @param podType the type of pod to use. A string with one of s1, p1, or p2 appended with a "." and one of x1, x2, x4, or x8.
     * @param metric the metric type for the index. Must be one of "cosine", "euclidean", or "dot_product".
     * @param metadataConfig the configuration for the behavior of Pinecone's internal metadata index. By default, all metadata is indexed;
     *                       when metadataConfig is present, only specified metadata fields are indexed.
     * @return {@link IndexModel} representing the created serverless index.
     * @throws PineconeException if the API encounters an error during index creation or if any of the arguments are invalid.
     */
    public IndexModel createPodsIndex(String indexName, Integer dimension, String environment,
                                      String podType, String metric, CreateIndexRequestSpecPodMetadataConfig metadataConfig) {
        return createPodsIndex(indexName, dimension, environment, podType, metric, null, null, null, metadataConfig,
                null);
    }

    /**
     * Overload for creating a new pods index with environment, podType, metric, and sourceCollection.
     *
     * <pre>
     *     import io.pinecone.clients.Pinecone;
     *
     *     Pinecone client = new Pinecone.Builder(System.getenv("PINECONE_API_KEY")).build();
     *
     *     client.createPodsIndex("my-index", 1536, "us-east4-gcp", "p1.x2", "cosine", "my-collection");
     * </pre>
     *
     * @param indexName the name of the index to be created.
     * @param dimension the number of dimensions for the index.
     * @param environment the cloud environment where you want the index to be hosted.
     * @param podType the type of pod to use. A string with one of s1, p1, or p2 appended with a "." and one of x1, x2, x4, or x8.
     * @param metric the metric type for the index. Must be one of "cosine", "euclidean", or "dot_product".
     * @param sourceCollection the name of the collection to be used as the source for the index. Collections are snapshots of an index at a point in time.
     * @return {@link IndexModel} representing the created serverless index.
     * @throws PineconeException if the API encounters an error during index creation or if any of the arguments are invalid.
     */
    public IndexModel createPodsIndex(String indexName, Integer dimension, String environment,
                                      String podType, String metric, String sourceCollection) {
        return createPodsIndex(indexName, dimension, environment, podType, metric, null, null, null, null,
                sourceCollection);
    }

    /**
     * Overload for creating a new pods index with environment, podType, and pods.
     *
     * <pre>
     *     import io.pinecone.clients.Pinecone;
     *
     *     Pinecone client = new Pinecone.Builder(System.getenv("PINECONE_API_KEY")).build();
     *
     *     client.createPodsIndex("my-index", 1536, "us-east4-gcp", "p1.x2", "cosine", 6);
     * </pre>
     *
     * @param indexName the name of the index to be created.
     * @param dimension the number of dimensions for the index.
     * @param environment the cloud environment where you want the index to be hosted.
     * @param podType the type of pod to use. A string with one of s1, p1, or p2 appended with a "." and one of x1, x2, x4, or x8.
     * @param pods the number of pods to be used in the index.
     * @return {@link IndexModel} representing the created serverless index.
     * @throws PineconeException if the API encounters an error during index creation or if any of the arguments are invalid.
     */
    public IndexModel createPodsIndex(String indexName, Integer dimension, String environment,
                                      String podType, Integer pods) {
        return createPodsIndex(indexName, dimension, environment, podType, null, null, null, pods, null, null);
    }

    /**
     * Overload for creating a new pods index with environment, podType, pods, and metadataConfig.
     *
     * <pre>
     *     import io.pinecone.clients.Pinecone;
     *     import org.openapitools.client.model.CreateIndexRequestSpecPodMetadataConfig;
     *
     *     Pinecone client = new Pinecone.Builder(System.getenv("PINECONE_API_KEY")).build();
     *
     *     CreateIndexRequestSpecPodMetadataConfig metadataConfig = new CreateIndexRequestSpecPodMetadataConfig().fields(Arrays.asList("genre", "year"));
     *     client.createPodsIndex("my-index", 1536, "us-east4-gcp", "p1.x2", "cosine", 6);
     * </pre>
     *
     * @param indexName the name of the index to be created.
     * @param dimension the number of dimensions for the index.
     * @param environment the cloud environment where you want the index to be hosted.
     * @param podType the type of pod to use. A string with one of s1, p1, or p2 appended with a "." and one of x1, x2, x4, or x8.
     * @param pods the number of pods to be used in the index.
     * @return {@link IndexModel} representing the created serverless index.
     * @throws PineconeException if the API encounters an error during index creation or if any of the arguments are invalid.
     */
    public IndexModel createPodsIndex(String indexName, Integer dimension, String environment,
                                      String podType, Integer pods,
                                      CreateIndexRequestSpecPodMetadataConfig metadataConfig) {
        return createPodsIndex(indexName, dimension, environment, podType, null, null, null, pods, metadataConfig,
                null);
    }

    /**
     * Overload for creating a new pods index with environment, podType, replicas, and shards.
     *
     * <pre>
     *     import io.pinecone.clients.Pinecone;
     *
     *     Pinecone client = new Pinecone.Builder(System.getenv("PINECONE_API_KEY")).build();
     *
     *     client.createPodsIndex("my-index", 1536, "us-east4-gcp", "p1.x2", "cosine", 2, 2);
     * </pre>
     *
     * @param indexName the name of the index to be created.
     * @param dimension the number of dimensions for the index.
     * @param environment the cloud environment where you want the index to be hosted.
     * @param podType the type of pod to use. A string with one of s1, p1, or p2 appended with a "." and one of x1, x2, x4, or x8.
     * @param replicas the number of replicas. Replicas duplicate your index. They provide higher availability and throughput and can be scaled.
     * @param shards the number of shards. Shards split your data across multiple pods so you can fit more data into an index.
     * @return {@link IndexModel} representing the created serverless index.
     * @throws PineconeException if the API encounters an error during index creation or if any of the arguments are invalid.
     */
    public IndexModel createPodsIndex(String indexName, Integer dimension, String environment,
                                      String podType, Integer replicas,
                                      Integer shards) {
        return createPodsIndex(indexName, dimension, environment, podType, null, replicas, shards, null, null, null);
    }

    /**
     * Overload for creating a new pods index with environment, podType, replicas, shards, and metadataConfig.
     *
     * <pre>
     *     import io.pinecone.clients.Pinecone;
     *     import org.openapitools.client.model.CreateIndexRequestSpecPodMetadataConfig;
     *
     *     Pinecone client = new Pinecone.Builder(System.getenv("PINECONE_API_KEY")).build();
     *
     *     CreateIndexRequestSpecPodMetadataConfig metadataConfig = new CreateIndexRequestSpecPodMetadataConfig().fields(Arrays.asList("genre", "year"));
     *     client.createPodsIndex("my-index", 1536, "us-east4-gcp", "p1.x2", "cosine", 2, 2, metadataConfig);
     * </pre>
     *
     * @param indexName the name of the index to be created.
     * @param dimension the number of dimensions for the index.
     * @param environment the cloud environment where you want the index to be hosted.
     * @param podType the type of pod to use. A string with one of s1, p1, or p2 appended with a "." and one of x1, x2, x4, or x8.
     * @param replicas the number of replicas. Replicas duplicate your index. They provide higher availability and throughput and can be scaled.
     * @param shards the number of shards. Shards split your data across multiple pods so you can fit more data into an index.
     * @return {@link IndexModel} representing the created serverless index.
     * @throws PineconeException if the API encounters an error during index creation or if any of the arguments are invalid.
     */
    public IndexModel createPodsIndex(String indexName, Integer dimension, String environment,
                                      String podType, Integer replicas,
                                      Integer shards, CreateIndexRequestSpecPodMetadataConfig metadataConfig) {
        return createPodsIndex(indexName, dimension, environment, podType, null, replicas, shards, null,
                metadataConfig,
                null);
    }

    /**
     * Creates a new pods index with the specified parameters.
     *
     * <pre>
     *     import io.pinecone.clients.Pinecone;
     *     import org.openapitools.client.model.CreateIndexRequestSpecPodMetadataConfig;
     *
     *     Pinecone client = new Pinecone.Builder(System.getenv("PINECONE_API_KEY")).build();
     *
     *     CreateIndexRequestSpecPodMetadataConfig metadataConfig = new CreateIndexRequestSpecPodMetadataConfig().fields(Arrays.asList("genre", "year"));
     *     client.createPodsIndex("my-index", 1536, "us-east4-gcp", "p1.x2", "cosine", 2, 2, 4, null, null);
     * </pre>
     *
     * @param indexName the name of the index to be created.
     * @param dimension the number of dimensions for the index.
     * @param environment the cloud environment where you want the index to be hosted.
     * @param podType the type of pod to use. A string with one of s1, p1, or p2 appended with a "." and one of x1, x2, x4, or x8.
     * @param metric the metric type for the index. Must be one of "cosine", "euclidean", or "dot_product".
     * @param replicas the number of replicas. Replicas duplicate your index. They provide higher availability and throughput and can be scaled.
     * @param shards the number of shards. Shards split your data across multiple pods so you can fit more data into an index.
     * @param pods the number of pods to be used in the index. This should be equal to shards x replicas.
     * @param metadataConfig the configuration for the behavior of Pinecone's internal metadata index. By default, all metadata is indexed;
     *                       when metadataConfig is present, only specified metadata fields are indexed.
     * @param sourceCollection the name of the collection to be used as the source for the index. Collections are snapshots of an index at a point in time.
     * @return {@link IndexModel} representing the created serverless index.
     * @throws PineconeException if the API encounters an error during index creation or if any of the arguments are invalid.
     */
    public IndexModel createPodsIndex(String indexName, Integer dimension, String environment,
                                      String podType, String metric,
                                      Integer replicas, Integer shards, Integer pods,
                                      CreateIndexRequestSpecPodMetadataConfig metadataConfig, String sourceCollection) throws PineconeException {
        validatePodIndexParams(indexName, dimension, environment, podType, metric, replicas, shards, pods);

        CreateIndexRequestSpecPod podSpec = new CreateIndexRequestSpecPod().environment(environment)
                .podType(podType)
                .replicas(replicas)
                .shards(shards)
                .pods(pods)
                .metadataConfig(metadataConfig)
                .sourceCollection(sourceCollection);
        CreateIndexRequestSpec createIndexRequestSpec = new CreateIndexRequestSpec().pod(podSpec);
        CreateIndexRequest createIndexRequest = new CreateIndexRequest()
                .name(indexName)
                .dimension(dimension)
                .metric(metric != null ? IndexMetric.fromValue(metric) : IndexMetric.COSINE)
                .spec(createIndexRequestSpec);

        IndexModel indexModel = null;
        try {
            indexModel = manageIndexesApi.createIndex(createIndexRequest);
        } catch (ApiException apiException) {
            handleApiException(apiException);
        }
        return indexModel;
    }


    public static void validatePodIndexParams(String indexName, Integer dimension, String environment,
                                              String podType, String metric,
                                              Integer replicas, Integer shards, Integer pods) {

        if (indexName == null || indexName.isEmpty()) {
            throw new PineconeValidationException("indexName cannot be null or empty");
        }

        if (dimension == null) {
            throw new PineconeValidationException("Dimension cannot be null");
        }

        if (dimension < 1) {
            throw new PineconeValidationException("Dimension must be greater than 0. See limits for more info: https://docs.pinecone.io/reference/limits");
        }

        if (environment == null || environment.isEmpty()) {
            throw new PineconeValidationException("Environment cannot be null or empty");
        }

        if (podType == null || podType.isEmpty()) {
            throw new PineconeValidationException("podType cannot be null or empty");
        }

        if (metric != null && metric.isEmpty()) {
            throw new PineconeValidationException("Metric cannot be null or empty. Must be one of " + Arrays.toString(IndexMetric.values()));
        }

        if (replicas != null && replicas < 1) {
            throw new PineconeValidationException("Number of replicas must be >= 1");
        }

        if (shards != null && shards < 1) {
            throw new PineconeValidationException("Number of shards must be >= 1");
        }

        if (pods != null && pods < 1) {
            throw new PineconeValidationException("Number of pods must be >= 1");
        }

        if (replicas != null && shards != null && pods != null && (replicas*shards != pods)) {
            throw new PineconeValidationException("Number of pods does not equal number of shards times number of " +
                    "replicas"); }
    }

    /**
     * Retrieves information about an existing index.
     *
     * <pre>
     *     import io.pinecone.clients.Pinecone;
     *     import org.openapitools.client.model.IndexModel;
     *
     *     Pinecone client = new Pinecone.Builder(System.getenv("PINECONE_API_KEY")).build();
     *
     *     IndexModel index = client.describeIndex("my-index");
     *     System.out.println("Your index is hosted at: " + index.getHost());
     * </pre>
     *
     * @param indexName the name of the index to describe.
     * @return {@link IndexModel} with the details of the index.
     * @throws PineconeException if an error occurs during the operation or the index does not exist.
     */
    public IndexModel describeIndex(String indexName) throws PineconeException {
        IndexModel indexModel = null;
        try {
            indexModel = manageIndexesApi.describeIndex(indexName);
        } catch (ApiException apiException) {
            handleApiException(apiException);
        }
        return indexModel;
    }

    /**
     * Configures an existing pod-based index with new settings.
     *
     * <pre>
     *     import io.pinecone.clients.Pinecone;
     *     import org.openapitools.client.model.IndexModel;
     *
     *     Pinecone client = new Pinecone.Builder(System.getenv("PINECONE_API_KEY")).build();
     *
     *     // Make a configuration change
     *     IndexModel index = client.configureIndex("my-index", "p1.x2", 4);
     *
     *     // Call describeIndex to see the index status as the change is applied.
     *     index = client.describeIndex("my-index");
     * </pre>
     *
     * @param indexName the name of the index to configure.
     * @param podType the new podType for the index. Can be null if not changing the pod type.
     * @param replicas the desired number of replicas for the index, lowest value is 0. Can be null if not changing the number of replicas.
     * @return {@link IndexModel} representing the configured index.
     * @throws PineconeException if an error occurs during the operation, the index does not exist, or if any of the arguments are invalid.
     */
    public IndexModel configureIndex(String indexName, String podType, Integer replicas) throws PineconeException {
        if (indexName == null || indexName.isEmpty()) {
            throw new PineconeValidationException("indexName cannot be null or empty");
        }

        if (podType == null && replicas == null) {
            throw new PineconeValidationException("Must provide either podType or replicas");
        }

        // If you pass a # replicas, but they're < 1, throw an exception
        if (replicas != null) {
            if (replicas < 1) {
                throw new PineconeValidationException("Number of replicas must be >= 1");
            }
        }

        // Build ConfigureIndexRequest object
        ConfigureIndexRequest configureIndexRequest = new ConfigureIndexRequest()
                .spec(new ConfigureIndexRequestSpec()
                        .pod(new ConfigureIndexRequestSpecPod()
                                .replicas(replicas)
                                .podType(podType)
                        )
                );

        IndexModel indexModel = null;
        try {
            indexModel = manageIndexesApi.configureIndex(indexName, configureIndexRequest);
        } catch (ApiException apiException) {
            handleApiException(apiException);
        }
        return indexModel;
    }

    /**
     * Overload for configureIndex to only change the number of replicas for an index.
     *
     * <pre>
     *     import io.pinecone.clients.Pinecone;
     *     import org.openapitools.client.model.IndexModel;
     *
     *     Pinecone client = new Pinecone.Builder(System.getenv("PINECONE_API_KEY")).build();
     *
     *     IndexModel index = client.configureIndex("my-index", 4);
     * </pre>
     *
     * @param indexName the name of the index.
     * @param replicas the desired number of replicas for the index, lowest value is 0.
     * @return {@link IndexModel} of the configured index.
     * @throws PineconeException if an error occurs during the operation, the index does not exist, or if the number of replicas is invalid.
     */
    public IndexModel configureIndex(String indexName, Integer replicas) throws PineconeException {
        return configureIndex(indexName, null, replicas);
    }

    /**
     * Overload for configureIndex to only change the podType of an index.
     *
     * <pre>
     *     import io.pinecone.clients.Pinecone;d
     *     import org.openapitools.client.model.IndexModel;
     *
     *     Pinecone client = new Pinecone.Builder(System.getenv("PINECONE_API_KEY")).build();
     *
     *     IndexModel index = client.configureIndex("my-index", "p1.x2");
     * </pre>
     *
     * @param indexName the name of the index.
     * @param podType the new podType for the index.
     * @return {@link IndexModel} of the configured index.
     * @throws PineconeException if an error occurs during the operation, the index does not exist, or if the podType is invalid.
     */
    public IndexModel configureIndex(String indexName, String podType) throws PineconeException {
        return configureIndex(indexName, podType, null);
    }

    /**
     * Lists all indexes in your project, including the index name, dimension, metric, status, and spec.
     *
     * <pre>
     *     import io.pinecone.clients.Pinecone;
     *     import org.openapitools.client.model.IndexList;
     *
     *     Pinecone client = new Pinecone.Builder(System.getenv("PINECONE_API_KEY")).build();
     *
     *     IndexList indexes = client.listIndexes();
     * </pre>
     *
     * @return {@link IndexList} containing all indexes.
     * @throws PineconeException if an error occurs during the operation.
     */
    public IndexList listIndexes() throws PineconeException {
        IndexList indexList = null;
        try {
            indexList = manageIndexesApi.listIndexes();
        } catch (ApiException apiException) {
            handleApiException(apiException);
        }
        return indexList;
    }

    /**
     * Deletes an index.
     * <p>
     * Deleting an index is an irreversible operation. All data in the index will be lost.
     * When you use this command, a request is sent to the Pinecone control plane to delete
     * the index, but the termination is not synchronous because resources take a few moments to
     * be released.
     * <p>
     * You can check the status of the index by calling the describeIndex command.
     * With repeated polling of the describeIndex command, you will see the index transition to a
     * Terminating state before eventually resulting in a 404 after it has been removed.
     *
     * <pre>
     *     import io.pinecone.clients.Pinecone;
     *     import org.openapitools.client.model.IndexModel;
     *
     *     Pinecone client = new Pinecone.Builder(System.getenv("PINECONE_API_KEY")).build();
     *
     *     // Delete an index
     *     client.deleteIndex("my-index");
     *
     *     // Verify index status with describeIndex
     *     IndexModel index = client.describeIndex("my-index");
     *     System.out.println("Index status: " + index.getStatus().getState());
     * </pre>
     *
     * @param indexName the name of the index to delete.
     * @throws PineconeException if an error occurs during the deletion operation or the index does not exist.
     */
    public void deleteIndex(String indexName) throws PineconeException {
        try {
            manageIndexesApi.deleteIndex(indexName);
        } catch (ApiException apiException) {
            handleApiException(apiException);
        }
    }

    /**
     * Creates a new collection from a source index.
     *
     * <pre>
     *     import io.pinecone.clients.Pinecone;
     *     import org.openapitools.client.model.CollectionModel;
     *
     *     Pinecone client = new Pinecone.Builder(System.getenv("PINECONE_API_KEY")).build();
     *
     *     CollectionModel collection = client.createCollection("my-collection", "my-source-index");
     * </pre>
     *
     * @param collectionName the name of the new collection.
     * @param sourceIndex the name of the source index.
     * @return {@link CollectionModel} representing the created collection.
     * @throws PineconeException if an error occurs during the operation, or the source collection is invalid.
     */
    public CollectionModel createCollection(String collectionName, String sourceIndex) throws PineconeException {
        if (collectionName == null || collectionName.isEmpty()) {
            throw new PineconeValidationException("collectionName cannot be null or empty");
        }
        if (sourceIndex == null || sourceIndex.isEmpty()) {
            throw new PineconeValidationException("sourceIndex cannot be null or empty");
        }

        CreateCollectionRequest createCollectionRequest = new CreateCollectionRequest()
                .name(collectionName)
                .source(sourceIndex);

        CollectionModel collection = null;
        try {
            collection = manageIndexesApi.createCollection(createCollectionRequest);
        } catch (ApiException apiException) {
            handleApiException(apiException);
        }
        return collection;
    }

    /**
     * Describes an existing collection.
     *
     * <pre>
     *     import io.pinecone.clients.Pinecone;
     *     import org.openapitools.client.model.CollectionModel;
     *
     *     Pinecone client = new Pinecone.Builder(System.getenv("PINECONE_API_KEY")).build();
     *
     *     CollectionModel collection = client.describeCollection("my-collection");
     * </pre>
     *
     * @param collectionName the name of the collection to describe.
     * @return {@link CollectionModel} with the description of the collection.
     * @throws PineconeException if an error occurs during the operation or the collection does not exist.
     */
    public CollectionModel describeCollection(String collectionName) throws PineconeException {
        CollectionModel collection = null;
        try {
            collection = manageIndexesApi.describeCollection(collectionName);
        } catch (ApiException apiException) {
            handleApiException(apiException);
        }
        return collection;
    }

    /**
     * Lists all collections in the project.
     *
     * <pre>
     *     import io.pinecone.clients.Pinecone;
     *     import org.openapitools.client.model.CollectionList;
     *
     *     Pinecone client = new Pinecone.Builder(System.getenv("PINECONE_API_KEY")).build();
     *
     *     CollectionList collections = client.listCollections();
     * </pre>
     *
     * @return {@link CollectionList} containing all collections.
     * @throws PineconeException if an error occurs during the listing operation.
     */
    public CollectionList listCollections() throws PineconeException {
        CollectionList collections = null;
        try {
            collections = manageIndexesApi.listCollections();
        } catch (ApiException apiException) {
            handleApiException(apiException);
        }
        return collections;
    }

    /**
     * Deletes a collection.
     * <p>
     * Deleting a collection is an irreversible operation. All data in the collection will be lost.
     * This method tells Pinecone you would like to delete a collection, but it takes a few moments to complete the operation.
     * Use the describeCollection() method to confirm that the collection has been deleted.
     *
     * <pre>
     *     import io.pinecone.clients.Pinecone;
     *
     *     Pinecone client = new Pinecone.Builder(System.getenv("PINECONE_API_KEY")).build();
     *
     *     client.deleteCollection('my-collection');
     *
     *     // Verify collection status with describeCollection
     *     client.describeCollection('my-collection');
     * </pre>
     *
     * @param collectionName the name of the collection to delete.
     * @throws PineconeException if an error occurs during the deletion operation or the collection does not exist.
     */
    public void deleteCollection(String collectionName) throws PineconeException {
        try {
            manageIndexesApi.deleteCollection(collectionName);
        } catch (ApiException apiException) {
            handleApiException(apiException);
        }
    }

    /**
     * Retrieves a connection to a specific index for synchronous operations. This method initializes
     * and returns an {@link Index} object that represents a connection to an index and allowing for
     * synchronous operations against it.
     *
     * <pre>
     *     import io.pinecone.clients.Pinecone;
     *     import io.pinecone.clients.Index;
     *
     *     Pinecone client = new Pinecone.Builder(System.getenv("PINECONE_API_KEY")).build();
     *     Index index = client.getIndexConnection("my-index");
     *
     *     // Use the index object to interact with the index
     *     index.describeIndexStats();
     * </pre>
     *
     * @param indexName The name of the index to connect to. Must not be null or empty.
     * @return An {@link Index} object representing the connection to the specified index.
     * @throws PineconeValidationException If the indexName is null or empty.
     */
    public Index getIndexConnection(String indexName) throws PineconeValidationException {
        if(indexName == null || indexName.isEmpty()) {
            throw new PineconeValidationException("Index name cannot be null or empty");
        }

        config.setHost(getIndexHost(indexName));
        PineconeConnection connection = getConnection(indexName);
        return new Index(connection, indexName);
    }

    /**
     * Retrieves a connection to a specific index for asynchronous operations. This method initializes
     * and returns an {@link AsyncIndex} object that represents a connection to an index and allowing for
     * synchronous operations against it.
     *
     * <pre>
     *     import io.pinecone.clients.Pinecone;
     *     import io.pinecone.clients.AsyncIndex;
     *
     *     Pinecone client = new Pinecone.Builder(System.getenv("PINECONE_API_KEY")).build();
     *     AsyncIndex asyncIndex = client.getAsyncIndexConnection("my-index");
     *
     *     // Use the index object to interact with the index
     *     asyncIndex.describeIndexStats();
     * </pre>
     *
     * @param indexName The name of the index to connect to. Must not be null or empty.
     * @return An {@link AsyncIndex} object representing the connection to the specified index.
     * @throws PineconeValidationException If the indexName is null or empty.
     */
    public AsyncIndex getAsyncIndexConnection(String indexName) throws PineconeValidationException {
        if(indexName == null || indexName.isEmpty()) {
            throw new PineconeValidationException("Index name cannot be null or empty");
        }

        config.setHost(getIndexHost(indexName));
        PineconeConnection connection = getConnection(indexName);
        return new AsyncIndex(connection, indexName);
    }

    PineconeConnection getConnection(String indexName) {
        return connectionsMap.computeIfAbsent(indexName, key -> new PineconeConnection(config));
    }

    ConcurrentHashMap<String, PineconeConnection> getConnectionsMap() {
        return connectionsMap;
    }

    String getIndexHost(String indexName) {
        return this.describeIndex(indexName).getHost();
    }

    static void closeConnection(String indexName) {
        connectionsMap.remove(indexName);
    }

    private void handleApiException(ApiException apiException) throws PineconeException {
        int statusCode = apiException.getCode();
        String responseBody = apiException.getResponseBody();
        FailedRequestInfo failedRequestInfo = new FailedRequestInfo(statusCode, responseBody);
        HttpErrorMapper.mapHttpStatusError(failedRequestInfo);
    }


    /**
     * A builder class for creating a {@link Pinecone} instance. This builder allows for configuring a {@link Pinecone}
     * instance with custom parameters including an API key, a source tag, and a custom OkHttpClient.
     */
    public static class Builder {
        // Required fields
        private final String apiKey;

        // Optional fields
        private String sourceTag;
        private OkHttpClient okHttpClient = new OkHttpClient();

        /**
         * Constructs a new {@link Builder} with the mandatory API key.
         *
         * <pre>
         *     import io.pinecone.clients.Pinecone;
         *     import org.openapitools.client.model.IndexList;
         *
         *     Pinecone client = new Pinecone.Builder(System.getenv("PINECONE_API_KEY")).build();
         *
         *     // Use the client to interact with Pinecone
         *     IndexList indexes = client.listIndexes();
         * </pre>
         *
         * @param apiKey The API key required for authenticating requests to Pinecone services.
         */
        public Builder(String apiKey) {
            this.apiKey = apiKey;
        }

        /**
         * Sets the source tag for the requests made by the Pinecone client.
         *
         * <pre>
         *     import io.pinecone.clients.Pinecone;
         *     import org.openapitools.client.model.IndexList;
         *
         *     Pinecone client = new Pinecone.Builder(System.getenv("PINECONE_API_KEY")).withSourceTag("my-source-tag").build();
         *
         *     // The tag will be included as a header in all requests made by the client
         *     IndexList indexes = client.listIndexes();
         * </pre>
         *
         * @param sourceTag The source tag to identify the origin of the requests.
         * @return This {@link Builder} instance for chaining method calls.
         */
        public Builder withSourceTag(String sourceTag) {
            this.sourceTag = sourceTag;
            return this;
        }

        /**
         * Sets a custom OkHttpClient for the Pinecone client to use for making HTTP requests.
         *
         * <pre>
         *     import io.pinecone.clients.Pinecone;
         *     import org.openapitools.client.model.IndexList;
         *     import okhttp3.*;
         *
         *     OkHttpClient myClient = new OkHttpClient();
         *     Pinecone client = new Pinecone.Builder(System.getenv("PINECONE_API_KEY")).withOkHttpClient(myClient).build();
         *
         *     // Network requests will now be made using your custom OkHttpClient
         *     IndexList indexes = client.listIndexes();
         * </pre>
         *
         * @param okHttpClient The custom OkHttpClient to be used. Must not be null.
         * @return This {@link Builder} instance for chaining method calls.
         */
        public Builder withOkHttpClient(OkHttpClient okHttpClient) {
            this.okHttpClient = okHttpClient;
            return this;
        }

        /**
         * Builds and returns a {@link Pinecone} instance configured with the provided API key, optional source tag,
         * and OkHttpClient.
         * <p>
         * This method also performs configuration validation, sets up the internal API client, and manages indexes API
         * with debugging options based on the environment variables.
         *
         * @return A new {@link Pinecone} instance configured based on the builder parameters.
         */
        public Pinecone build() {
            PineconeConfig clientConfig = new PineconeConfig(apiKey);
            clientConfig.setSourceTag(sourceTag);
            clientConfig.validate();

            ApiClient apiClient = new ApiClient(okHttpClient);
            apiClient.setApiKey(clientConfig.getApiKey());
            apiClient.setUserAgent(clientConfig.getUserAgent());

            if (Boolean.parseBoolean(System.getenv("PINECONE_DEBUG"))) {
                apiClient.setDebugging(true);
            }

            ManageIndexesApi manageIndexesApi = new ManageIndexesApi();
            manageIndexesApi.setApiClient(apiClient);

            return new Pinecone(clientConfig, manageIndexesApi);
        }
    }
}