package io.pinecone;

import io.pinecone.model.CreateIndexRequest;
import okhttp3.*;
import java.io.IOException;

import static io.pinecone.utils.Constants.*;

public class PineconeIndexOperationClient {
    private final OkHttpClient client;
    private final PineconeClientConfig clientConfig;
    private final String url;

    PineconeIndexOperationClient(PineconeClientConfig clientConfig, OkHttpClient client) {
        this.clientConfig = clientConfig;
        this.client = client;
        this.url = BASE_URL_PREFIX + clientConfig.getEnvironment() + BASE_URL_SUFFIX;
    }

    public PineconeIndexOperationClient(PineconeClientConfig clientConfig) {
        this.clientConfig = clientConfig;
        this.client = new OkHttpClient();
        this.url = BASE_URL_PREFIX + clientConfig.getEnvironment() + BASE_URL_SUFFIX;
    }

    public void deleteIndex(String indexName) throws IOException {
        Request request = new Request.Builder()
                .url(url + indexName)
                .delete()
                .addHeader(ACCEPT_HEADER, TEXT_PLAIN)
                .addHeader(API_KEY, clientConfig.getApiKey())
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException(response.message());
            }
        }
        finally {
            close(client);
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
                .addHeader(API_KEY, clientConfig.getApiKey())
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException(response.message());
            }
        }
        finally {
            close(client);
        }
    }

    public void close(OkHttpClient client) {
        client.dispatcher().executorService().shutdown();
        client.connectionPool().evictAll();
    }
}
