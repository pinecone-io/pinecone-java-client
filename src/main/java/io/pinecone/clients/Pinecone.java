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

public class Pinecone {

    private final ManageIndexesApi manageIndexesApi;
    private final PineconeConfig config;

    private Pinecone(PineconeConfig config, ManageIndexesApi manageIndexesApi) {
        this.config = config;
        this.manageIndexesApi = manageIndexesApi;
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

    public IndexModel createIndex(CreateIndexRequest createIndexRequest) throws PineconeValidationException {
        if (createIndexRequest == null) {
            throw new PineconeValidationException("CreateIndexRequest object cannot be null");
        }
        IndexModel indexModel = null;
        try {
            indexModel = manageIndexesApi.createIndex(createIndexRequest);
        } catch (ApiException apiException) {
            handleApiException(apiException);
        }
        return indexModel;
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

    public IndexModel configureIndex(String indexName, String podType, Integer replicas) throws PineconeValidationException {
        if (indexName == null || indexName.isEmpty()) {
            throw new PineconeValidationException("indexName cannot be null or empty");
        }

        // Set default values if parameters are not provided
        String defaultPodType = "p1.x1";
        int defaultReplicas = 1;

        // If you pass a # replicas, but they're < 1, throw an exception
        if (replicas != null) {
            if (replicas < 1) {
                throw new PineconeValidationException("Number of replicas must be >= 1");
            }
        }

        // If you pass some random string that's not an actual podType, throw an exception
        if (podType != null && !podType.isEmpty()) {
            if (!podType.startsWith("s") && !podType.startsWith("p")) {
                throw new PineconeValidationException("podType must start with 's' or 'p'");
            }
            if (!podType.endsWith("1") && !podType.endsWith("2") && !podType.endsWith("4") && !podType.endsWith("8")) {
                throw new PineconeValidationException("podType must end with either 1, 2, 3, or 4");
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

//    public IndexModel configureIndexOld(String indexName, ConfigureIndexRequest configureIndexRequest) throws PineconeValidationException {
//        if (configureIndexRequest == null) {
//            throw new PineconeValidationException("ConfigureIndexRequest object cannot be null");
//        }
//        if (indexName == null || indexName.isEmpty()) {
//            throw new PineconeValidationException("Index name cannot be null or empty");
//        }
//
//        IndexModel indexModel = null;
//        try {
//            indexModel = manageIndexesApi.configureIndex(indexName, configureIndexRequest);
//        } catch (ApiException apiException) {
//            handleApiException(apiException);
//        }
//        return indexModel;
//    }

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

    public CollectionModel createCollection(String collectionName, String sourceIndex) throws PineconeValidationException {
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

    public Index createIndexConnection(String indexName) {
        PineconeConnection connection = new PineconeConnection(config, indexName);
        return new Index(connection);
    }

    public AsyncIndex createAsyncIndexConnection(String indexName) {
        PineconeConnection connection = new PineconeConnection(config, indexName);
        return new AsyncIndex(connection);
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