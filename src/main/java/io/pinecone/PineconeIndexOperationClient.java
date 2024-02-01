package io.pinecone;

import io.pinecone.exceptions.FailedRequestInfo;
import io.pinecone.exceptions.HttpErrorMapper;
import io.pinecone.exceptions.PineconeException;
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.api.ManageIndexesApi;
import org.openapitools.client.model.*;

import okhttp3.*;
import java.io.IOException;

public class PineconeIndexOperationClient {
    private final ManageIndexesApi manageIndexesAPi;

    public PineconeIndexOperationClient(String apiKey, OkHttpClient okHttpClient) {
        ApiClient apiClient = new ApiClient(okHttpClient);
        apiClient.setApiKey(apiKey);
        this.manageIndexesAPi = new ManageIndexesApi(apiClient);
    }

    public PineconeIndexOperationClient(String apiKey) {
        this(apiKey, new OkHttpClient());
    }

    public void deleteIndex(String indexName) throws IOException {
        try {
            this.manageIndexesAPi.deleteIndex(indexName);
        }
        catch (ApiException e) {
            handleApiException(e);
        }
    }

    public IndexModel createIndex(CreateIndexRequest createIndexRequest) throws IOException {
        IndexModel createdIndex = null;
        try {
            this.manageIndexesAPi.createIndex(createIndexRequest);
        }
        catch (ApiException e) {
            handleApiException(e);
        }
        return createdIndex;
    }

    public IndexModel describeIndex(String indexName) throws IOException {
        IndexModel index = null;
        try {
            index = this.manageIndexesAPi.describeIndex(indexName);
        }
        catch (ApiException e) {
            handleApiException(e);
        }
        return index;
    }

    public IndexList listIndexes() throws ApiException {
        IndexList indexList = null;
        try {
            indexList = this.manageIndexesAPi.listIndexes();
        }
        catch (ApiException e) {
            handleApiException(e);
        }
        return indexList;
    }

    public IndexModel configureIndex(String indexName, ConfigureIndexRequest configureIndexRequest) throws IOException {
        IndexModel configuredIndex = null;
        try {
            this.manageIndexesAPi.configureIndex(indexName, configureIndexRequest);
        }
        catch (ApiException e) {
            handleApiException(e);
        }
        return configuredIndex;
    }

    public CollectionModel createCollection(CreateCollectionRequest createCollectionRequest) throws ApiException {
        CollectionModel createdCollection = null;
        try {
            createdCollection = this.manageIndexesAPi.createCollection(createCollectionRequest);
        }
        catch (ApiException e) {
            handleApiException(e);
        }
        return createdCollection;
    }

    public CollectionList listCollections() throws ApiException {
        CollectionList collectionList = null;
        try {
            collectionList = this.manageIndexesAPi.listCollections();
        }
        catch (ApiException e) {
            handleApiException(e);
        }
        return collectionList;
    }

    public CollectionModel describeCollection(String collectionName) throws ApiException {
        CollectionModel collection = null;
        try {
            collection = this.manageIndexesAPi.describeCollection(collectionName);
        }
        catch (ApiException e) {
            handleApiException(e);
        }
        return collection;
    }

    public String deleteCollection(String collectionName) throws ApiException {
        String deleteResponse = null;
        try {
            deleteResponse = this.manageIndexesAPi.deleteCollection(collectionName);
        }
        catch (ApiException e) {
            handleApiException(e);
        }
        return deleteResponse;
    }

    private void handleApiException(ApiException exception) throws PineconeException {
        int statusCode = exception.getCode();
        String responseBody = exception.getResponseBody();
        FailedRequestInfo failedRequestInfo = new FailedRequestInfo(statusCode, responseBody);
        HttpErrorMapper.mapHttpStatusError(failedRequestInfo);
    }
}