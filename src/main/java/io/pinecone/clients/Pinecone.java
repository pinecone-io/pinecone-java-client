package io.pinecone.clients;

import io.pinecone.configs.PineconeConfig;
import io.pinecone.configs.PineconeConnection;
import io.pinecone.exceptions.FailedRequestInfo;
import io.pinecone.exceptions.HttpErrorMapper;
import io.pinecone.exceptions.PineconeException;
import io.pinecone.exceptions.PineconeValidationException;
import okhttp3.*;
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.api.ManageIndexesApi;
import org.openapitools.client.model.*;

public class Pinecone {

    private final ManageIndexesApi manageIndexesApi;
    private final PineconeConfig config;

    public Pinecone(String apiKey) {
        this(apiKey, new OkHttpClient());
    }

    public Pinecone(String apiKey, String sourceTag) {
        this(new PineconeConfig(apiKey), sourceTag, new OkHttpClient());
    }

    public Pinecone(String apiKey, OkHttpClient okHttpClient) {
        this(new PineconeConfig(apiKey), null, okHttpClient);
    }

    public Pinecone(String apiKey, String sourceTag, OkHttpClient okHttpClient) {
        this(new PineconeConfig(apiKey), sourceTag, okHttpClient);
    }
    public Pinecone(PineconeConfig pineconeConfig) {
        this(pineconeConfig, null, new OkHttpClient());
    }

    public Pinecone(PineconeConfig pineconeConfig, String sourceTag) {
        this(pineconeConfig, sourceTag, new OkHttpClient());
    }

    public Pinecone(PineconeConfig pineconeConfig, OkHttpClient okHttpClient) {
        this(pineconeConfig, null, okHttpClient);
    }

    public Pinecone(PineconeConfig pineconeConfig, String sourceTag, OkHttpClient okHttpClient) {
        pineconeConfig.setSourceTag(sourceTag);
        pineconeConfig.validate();
        config = pineconeConfig;

        ApiClient apiClient = new ApiClient(okHttpClient);
        apiClient.setApiKey(config.getApiKey());
        apiClient.setUserAgent(config.getUserAgent());

        if (Boolean.parseBoolean(System.getenv("PINECONE_DEBUG"))) {
            apiClient.setDebugging(true);
        }

        manageIndexesApi = new ManageIndexesApi();
        manageIndexesApi.setApiClient(apiClient);
    }

    public IndexModel createIndex(CreateIndexRequest createIndexRequest) throws PineconeException {
        IndexModel indexModel = new IndexModel();

        try {
            indexModel = manageIndexesApi.createIndex(createIndexRequest);
        } catch (ApiException apiException) {
            handleApiException(apiException);
        }
        return indexModel;
    }

    public IndexModel describeIndex(String indexName) throws PineconeException {
        IndexModel indexModel = new IndexModel();
        try {
            indexModel = manageIndexesApi.describeIndex(indexName);
        } catch (ApiException apiException) {
            handleApiException(apiException);
        }
        return indexModel;
    }

    public IndexModel configureIndex(String indexName, ConfigureIndexRequest configureIndexRequest) throws PineconeException {
        IndexModel indexModel = new IndexModel();
        try {
            indexModel = manageIndexesApi.configureIndex(indexName, configureIndexRequest);
        } catch (ApiException apiException) {
            handleApiException(apiException);
        }
        return indexModel;
    }

    public IndexList listIndexes() throws PineconeException {
        IndexList indexList = new IndexList();
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

    public CollectionModel createCollection(CreateCollectionRequest createCollectionRequest) throws PineconeException {
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
}