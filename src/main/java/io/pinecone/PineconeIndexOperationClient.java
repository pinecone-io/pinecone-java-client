package io.pinecone;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.pinecone.exceptions.FailedRequestInfo;
import io.pinecone.exceptions.HttpErrorMapper;
import io.pinecone.exceptions.PineconeConfigurationException;
import io.pinecone.model.ConfigureIndexRequest;
import io.pinecone.model.CreateIndexRequest;
import io.pinecone.model.IndexMeta;
import okhttp3.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
    public static final String CONTENT_TYPE_CHARSET_UTF8 = "charset=utf-8";
    public static final String EMPTY_RESOURCE_PATH = "";
    public static final String HTTP_METHOD_DELETE = "DELETE";
    public static final String HTTP_METHOD_GET = "GET";
    public static final String HTTP_METHOD_PATCH = "HTTP_METHOD_PATCH";
    public static final String HTTP_METHOD_POST = "POST";
    public static final String TEXT_PLAIN = "text/plain";
    public static final String USER_AGENT_HEADER = "User-Agent";


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
        Request request = buildRequest(HTTP_METHOD_DELETE, indexName, TEXT_PLAIN, null);
        try (Response response = client.newCall(request).execute()) {
            handleResponse(response);
        }
    }

    public void createIndex(CreateIndexRequest createIndexRequest) throws IOException {
        MediaType mediaType = MediaType.parse(CONTENT_TYPE_JSON + ";" + CONTENT_TYPE_CHARSET_UTF8);
        RequestBody requestBody = RequestBody.create(createIndexRequest.toJson(), mediaType);
        Request request = buildRequest(HTTP_METHOD_POST, EMPTY_RESOURCE_PATH, TEXT_PLAIN, requestBody);
        try (Response response = client.newCall(request).execute()) {
            handleResponse(response);
        }
    }

    public IndexMeta describeIndex(String indexName) throws IOException {
        Request request = buildRequest(HTTP_METHOD_GET, indexName, CONTENT_TYPE_JSON, null);
        try (Response response = client.newCall(request).execute()) {
            handleResponse(response);
            return new IndexMeta().fromJsonString(response.body().string());
        }
    }

    public List<String> listIndexes() throws IOException {
        String acceptHeaders = CONTENT_TYPE_JSON + "; " + CONTENT_TYPE_CHARSET_UTF8;
        List<String> indexList;
        ObjectMapper objectMapper = new ObjectMapper();
        Request request = buildRequest(HTTP_METHOD_GET, EMPTY_RESOURCE_PATH, acceptHeaders, null);
        try (Response response = client.newCall(request).execute()) {
            handleResponse(response);
            String responseBodyString = response.body().string();
            indexList = objectMapper.readValue(responseBodyString, ArrayList.class);
        }
        return indexList;
    }

    public void configureIndex(String indexName, ConfigureIndexRequest configureIndexRequest) throws IOException {
        MediaType mediaType = MediaType.parse(CONTENT_TYPE_JSON + ";" + CONTENT_TYPE_CHARSET_UTF8);
        RequestBody requestBody = RequestBody.create(configureIndexRequest.toJson(), mediaType);
        Request request = buildRequest(HTTP_METHOD_PATCH, indexName, TEXT_PLAIN, requestBody);
        try (Response response = client.newCall(request).execute()) {
            handleResponse(response);
        }
    }

    private Request buildRequest(String method, String path, String acceptHeader, RequestBody requestBody) {
        Request.Builder builder = new Request.Builder()
                .url(url + path)
                .addHeader(ACCEPT_HEADER, acceptHeader)
                .addHeader(API_KEY_HEADER_NAME, clientConfig.getApiKey())
                .addHeader(USER_AGENT_HEADER, clientConfig.getUserAgent());
        if (HTTP_METHOD_POST.equals(method)) {
            builder.post(requestBody);
            builder.addHeader(CONTENT_TYPE, CONTENT_TYPE_JSON);
        } else if(HTTP_METHOD_PATCH.equals(method)) {
            builder.patch(requestBody);
            builder.addHeader(CONTENT_TYPE, CONTENT_TYPE_JSON);
        } else if (HTTP_METHOD_DELETE.equals(method)) {
            builder.delete();
        }
        return builder.build();
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