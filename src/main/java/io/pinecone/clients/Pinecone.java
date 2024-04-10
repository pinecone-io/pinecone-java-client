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

public class Pinecone {

    private static final ConcurrentHashMap<String, PineconeConnection> connectionsMap = new ConcurrentHashMap<>();
    private final ManageIndexesApi manageIndexesApi;
    private final PineconeConfig config;

    Pinecone(PineconeConfig config, ManageIndexesApi manageIndexesApi) {
        this.config = config;
        this.manageIndexesApi = manageIndexesApi;
    }

//    public IndexModel createIndex(CreateIndexRequest createIndexRequest) throws PineconeValidationException {
//        if (createIndexRequest == null) {
//            throw new PineconeValidationException("CreateIndexRequest object cannot be null");
//        }
//        IndexModel indexModel = null;
//        try {
//            indexModel = manageIndexesApi.createIndex(createIndexRequest);
//        } catch (ApiException apiException) {
//            handleApiException(apiException);
//        }
//        return indexModel;
//    }

    public IndexModel createServerlessIndex(String indexName, String metric, int dimension, String cloud,
                                            String region) {
        if (indexName == null || indexName.isEmpty()) {
            throw new PineconeValidationException("Index name cannot be null or empty");
        }

        if (metric == null || metric.isEmpty()) {
            throw new PineconeValidationException("Metric cannot be null or empty. Must be one of " + Arrays.toString(IndexMetric.values()));
        }
        if (!(metric == null)) {
            try {
                IndexMetric.fromValue(metric.toLowerCase());
            } catch (IllegalArgumentException e) {
                throw new PineconeValidationException("Metric cannot be null or empty. Must be one of " + Arrays.toString(IndexMetric.values()));
            }
        }

        if (dimension < 1) {
            throw new PineconeValidationException("Dimension must be greater than 0. See limits for more info: https://docs.pinecone.io/reference/limits");
        }

        if (cloud == null || cloud.isEmpty()) {
            throw new PineconeValidationException("Cloud cannot be null or empty. Must be one of " + Arrays.toString(ServerlessSpec.CloudEnum.values()));
        }
        if (!(cloud == null)) {
            try {
                ServerlessSpec.CloudEnum.fromValue(cloud.toLowerCase());
            } catch (IllegalArgumentException e) {
                throw new PineconeValidationException("Cloud cannot be null or empty. Must be one of " + Arrays.toString(ServerlessSpec.CloudEnum.values()));
            }
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

    // Minimal
    public IndexModel createPodsIndex(String indexName, Integer dimension, String environment, String podType) {
        validatePodIndexParams(indexName, dimension, environment, podType, null, null, null, null);

        return createPodsIndex(indexName, dimension, environment, podType, null, null, null, null, null, null);
    }

    // Minimal + metric
    public IndexModel createPodsIndex(String indexName, Integer dimension, String environment,
                                      String podType, String metric) {
        validatePodIndexParams(indexName, dimension, environment, podType, metric, null, null, null);

        return createPodsIndex(indexName, dimension, environment, podType, metric, null, null, null, null, null);
    }

    // Minimal + metadata
    public IndexModel createPodsIndex(String indexName, Integer dimension, String environment,
                                      String podType, String metric, CreateIndexRequestSpecPodMetadataConfig metadataConfig) {
        validatePodIndexParams(indexName, dimension, environment, podType, metric, null, null, null);


        return createPodsIndex(indexName, dimension, environment, podType, metric, null, null, null, metadataConfig,
                null);
    }

    // Minimal + source collection
    public IndexModel createPodsIndex(String indexName, Integer dimension, String environment,
                                      String podType, String metric, String sourceCollection) {
        validatePodIndexParams(indexName, dimension, environment, podType, metric, null, null, null);

        return createPodsIndex(indexName, dimension, environment, podType, metric, null, null, null, null,
                sourceCollection);
    }

    // Minimal + pods
    public IndexModel createPodsIndex(String indexName, Integer dimension, String environment,
                                      String podType, Integer pods) {
        validatePodIndexParams(indexName, dimension, environment, podType, null, null, null, pods);

        return createPodsIndex(indexName, dimension, environment, podType, null, null, null, pods, null, null);
    }

    // Minimal + pods + metadata
    public IndexModel createPodsIndex(String indexName, Integer dimension, String environment,
                                      String podType, Integer pods,
                                      CreateIndexRequestSpecPodMetadataConfig metadataConfig) {
        validatePodIndexParams(indexName, dimension, environment, podType, null, null, null, pods);

        return createPodsIndex(indexName, dimension, environment, podType, null, null, null, pods, metadataConfig,
                null);
    }

    // Minimal + replicas, shards
    public IndexModel createPodsIndex(String indexName, Integer dimension, String environment,
                                      String podType, Integer replicas,
                                      Integer shards) {
        validatePodIndexParams(indexName, dimension, environment, podType, null, replicas, shards, null);

        return createPodsIndex(indexName, dimension, environment, podType, null, replicas, shards, null, null, null);
    }

    // Minimal + replicas, shards + metadata
    public IndexModel createPodsIndex(String indexName, Integer dimension, String environment,
                                      String podType, Integer replicas,
                                      Integer shards, CreateIndexRequestSpecPodMetadataConfig metadataConfig) {
        validatePodIndexParams(indexName, dimension, environment, podType, null, replicas, shards, null);

        return createPodsIndex(indexName, dimension, environment, podType, null, replicas, shards, null,
                metadataConfig,
                null);
    }

    // Max
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


    public IndexModel describeIndex(String indexName) throws PineconeException {
        IndexModel indexModel = null;
        try {
            indexModel = manageIndexesApi.describeIndex(indexName);
        } catch (ApiException apiException) {
            handleApiException(apiException);
        }
        return indexModel;
    }

    public IndexModel configureIndex(String indexName, String podType, Integer replicas) throws
            PineconeValidationException {
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

    // Overloaded method with indexName and replicas
    public IndexModel configureIndex(String indexName, Integer replicas) throws PineconeValidationException {
        return configureIndex(indexName, null, replicas);
    }

    // Overloaded method with indexName and podType
    public IndexModel configureIndex(String indexName, String podType) throws PineconeValidationException {
        return configureIndex(indexName, podType, null);
    }

    public IndexList listIndexes() throws PineconeException {
        IndexList indexList = null;
        try {
            indexList = manageIndexesApi.listIndexes();
        } catch (ApiException apiException) {
            handleApiException(apiException);
        }
        return indexList;
    }

    public void deleteIndex(String indexName) throws PineconeException {
        try {
            manageIndexesApi.deleteIndex(indexName);
        } catch (ApiException apiException) {
            handleApiException(apiException);
        }
    }

    public CollectionModel createCollection(String collectionName, String sourceIndex) throws
            PineconeValidationException {
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

    public CollectionModel describeCollection(String collectionName) throws PineconeException {
        CollectionModel collection = null;
        try {
            collection = manageIndexesApi.describeCollection(collectionName);
        } catch (ApiException apiException) {
            handleApiException(apiException);
        }
        return collection;
    }

    public CollectionList listCollections() throws PineconeException {
        CollectionList collections = null;
        try {
            collections = manageIndexesApi.listCollections();
        } catch (ApiException apiException) {
            handleApiException(apiException);
        }
        return collections;
    }

    public void deleteCollection(String collectionName) throws PineconeException {
        try {
            manageIndexesApi.deleteCollection(collectionName);
        } catch (ApiException apiException) {
            handleApiException(apiException);
        }
    }

    public Index getIndexConnection(String indexName) {
        if (indexName == null || indexName.isEmpty()) {
            throw new PineconeValidationException("Index name cannot be null or empty");
        }

        config.setHost(getIndexHost(indexName));
        PineconeConnection connection = getConnection(indexName);
        return new Index(connection, indexName);
    }

    public AsyncIndex getAsyncIndexConnection(String indexName) {
        if (indexName == null || indexName.isEmpty()) {
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

    public static class Builder {
        // Required parameters
        private final String apiKey;

        // Optional parameters
        private String sourceTag;
        private OkHttpClient okHttpClient = new OkHttpClient();

        public Builder(String apiKey) {
            this.apiKey = apiKey;
        }

        public Builder withSourceTag(String sourceTag) {
            this.sourceTag = sourceTag;
            return this;
        }

        public Builder withOkHttpClient(OkHttpClient okHttpClient) {
            this.okHttpClient = okHttpClient;
            return this;
        }

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