package io.pinecone.clients;

import io.pinecone.configs.PineconeConfig;
import okhttp3.OkHttpClient;
import org.openapitools.db_data.client.ApiClient;
import org.openapitools.db_data.client.ApiException;
import org.openapitools.db_data.client.Configuration;
import org.openapitools.db_data.client.api.VectorOperationsApi;
import org.openapitools.db_data.client.model.*;

import java.util.List;

import static io.pinecone.clients.Pinecone.buildOkHttpClient;

public class RestIndex {
    private final VectorOperationsApi vectorOperations;

    public RestIndex(PineconeConfig config) {
        OkHttpClient customOkHttpClient = config.getCustomOkHttpClient();
        ApiClient apiClient = (customOkHttpClient != null) ? new ApiClient(customOkHttpClient) : new ApiClient(buildOkHttpClient(config.getProxyConfig()));
        apiClient.setApiKey(config.getApiKey());
        apiClient.setUserAgent(config.getUserAgent());
        apiClient.addDefaultHeader("X-Pinecone-Api-Version", Configuration.VERSION);

        this.vectorOperations = new VectorOperationsApi(apiClient);
        String protocol = config.isTLSEnabled() ? "https://" : "http://";
        vectorOperations.setCustomBaseUrl(protocol + config.getHost());
    }

    public void upsertRecords(String namespace, List<UpsertRecord> upsertRecord) throws ApiException {
        vectorOperations.upsertRecordsNamespace(namespace, upsertRecord);
    }

    public SearchRecordsResponse searchRecords(String id, String namespace, Object filter, int topK, SearchRecordsVector vector) throws ApiException {
        SearchRecordsRequestQuery searchRecordsRequestquery = new SearchRecordsRequestQuery()
                .id(id)
                .filter(filter)
                .topK(topK)
                .vector(vector);
        SearchRecordsRequest request = new SearchRecordsRequest().query(searchRecordsRequestquery);

        return vectorOperations.searchRecordsNamespace(namespace, request);
    }
}
