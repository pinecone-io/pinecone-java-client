package io.pinecone;

import io.pinecone.exceptions.FailedRequestInfo;
import io.pinecone.exceptions.HttpErrorMapper;
import io.pinecone.exceptions.PineconeConfigurationException;
import io.pinecone.model.CreateIndexRequest;
import okhttp3.*;

import java.io.IOException;

public class PineconeIndexOperationClient {
    private final OkHttpClient client;
    private final PineconeClientConfig clientConfig;
    private final String url;
    public static final String ACCEPT_HEADER = "accept";
    public static final String API_KEY_HEADER_NAME = "Api-Key";
    public static final String BASE_URL_PREFIX = "https://controller.";
    public static final String BASE_URL_SUFFIX = ".pinecone.io/databases/";
    public static final String CONTENT_TYPE = "content-type";
    public static final String CONTENT_TYPE_JSON = "application/json";
    public static final String TEXT_PLAIN = "text/plain";

    private PineconeIndexOperationClient(PineconeClientConfig clientConfig, OkHttpClient client, String url) {
        this.client = client;
        this.clientConfig = clientConfig;
        this.url = url;
    }

    public PineconeIndexOperationClient(PineconeClientConfig clientConfig, OkHttpClient client) {
        this(clientConfig, client, createUrl(clientConfig));
    }

    public PineconeIndexOperationClient(PineconeClientConfig clientConfig) {
        this(clientConfig, new OkHttpClient());
    }

    private static String createUrl(PineconeClientConfig clientConfig) {
        if (clientConfig.getApiKey() == null || clientConfig.getEnvironment() == null) {
            throw new PineconeConfigurationException("Both API key and environment name are required for index operations.");
        }

        return BASE_URL_PREFIX + clientConfig.getEnvironment() + BASE_URL_SUFFIX;
    }

    public void deleteIndex(String indexName) throws IOException {
        Request request = new Request.Builder()
                .url(url + indexName)
                .delete()
                .addHeader(ACCEPT_HEADER, TEXT_PLAIN)
                .addHeader(API_KEY_HEADER_NAME, clientConfig.getApiKey())
                .build();

        try (Response response = client.newCall(request).execute()) {
            handleResponse(response);
        }
    }

    public void createIndex(CreateIndexRequest createIndexRequest) throws IOException {
        MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
        RequestBody requestBody = RequestBody.create(createIndexRequest.toJson(), mediaType);

        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .addHeader(ACCEPT_HEADER, TEXT_PLAIN)
                .addHeader(CONTENT_TYPE, CONTENT_TYPE_JSON)
                .addHeader(API_KEY_HEADER_NAME, clientConfig.getApiKey())
                .build();

        try (Response response = client.newCall(request).execute()) {
            handleResponse(response);
        }
    }

    private void handleResponse(Response response) throws IOException {
        if (!response.isSuccessful()) {
            int statusCode = response.code();
            String responseBodyString = (response.body() != null) ? response.body().string() : null;
            FailedRequestInfo failedRequestInfo = new FailedRequestInfo(statusCode, responseBodyString);
            HttpErrorMapper.mapHttpStatusError(failedRequestInfo);
        }
    }

    public void close() {
        client.dispatcher().executorService().shutdown();
        client.connectionPool().evictAll();
    }
}