package io.pinecone;

import io.pinecone.exceptions.FailedRequestInfo;
import io.pinecone.exceptions.HttpErrorMapper;
import io.pinecone.exceptions.PineconeValidationException;
import okhttp3.*;
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.api.ManageIndexesApi;
import org.openapitools.client.model.ConfigureIndexRequest;
import org.openapitools.client.model.CreateIndexRequest;
import org.openapitools.client.model.IndexList;
import org.openapitools.client.model.IndexModel;

public class PineconeControlPlaneClient {
    private ManageIndexesApi manageIndexesApi;

    public PineconeControlPlaneClient(String apiKey) {
        this(apiKey, new OkHttpClient());
    }

    public PineconeControlPlaneClient(String apiKey, OkHttpClient okHttpClient) {
        if(apiKey == null || apiKey.isEmpty()) {
            throw new PineconeValidationException("The API key is required and cannot be empty or null");
        }
        ApiClient apiClient = new ApiClient(okHttpClient);
        apiClient.setApiKey(apiKey);
        manageIndexesApi = new ManageIndexesApi();
        manageIndexesApi.setApiClient(apiClient);
    }

    public IndexModel createIndex(CreateIndexRequest createIndexRequest) {
        IndexModel indexModel = new IndexModel();
        try {
            manageIndexesApi.createIndex(createIndexRequest);
        } catch (ApiException apiException) {
            handleApiException(apiException);
        }
        return indexModel;
    }

    public IndexModel describeIndex(String indexName) {
        IndexModel indexModel = new IndexModel();
        try {
            indexModel = manageIndexesApi.describeIndex(indexName);
        } catch (ApiException apiException) {
            handleApiException(apiException);
        }
        return indexModel;
    }

    public void configureIndex(String indexName, ConfigureIndexRequest configureIndexRequest) {
        try {
            manageIndexesApi.configureIndex(indexName, configureIndexRequest);
        } catch (ApiException apiException) {
            handleApiException(apiException);
        }
    }

    public IndexList listIndexes() {
        IndexList indexList = new IndexList();
        try {
            indexList = manageIndexesApi.listIndexes();
        } catch (ApiException apiException) {
            handleApiException(apiException);
        }
        return indexList;
    }

    public void deleteIndex(String indexName) {
        try {
            manageIndexesApi.deleteIndex(indexName);
        } catch (ApiException apiException) {
            handleApiException(apiException);
        }
    }

    private void handleApiException(ApiException apiException) {
        int statusCode = apiException.getCode();
        String responseBody = apiException.getResponseBody();
        FailedRequestInfo failedRequestInfo = new FailedRequestInfo(statusCode, responseBody);
        HttpErrorMapper.mapHttpStatusError(failedRequestInfo);
    }
}