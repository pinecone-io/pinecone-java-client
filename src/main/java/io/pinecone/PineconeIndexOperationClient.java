package io.pinecone;

import io.pinecone.model.CreateIndexRequest;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClient;

import java.io.IOException;

public class PineconeIndexOperationClient {
    private AsyncHttpClient client;
    private PineconeClientConfig clientConfig;
    private String url;

    PineconeIndexOperationClient(PineconeClientConfig clientConfig, AsyncHttpClient client) {
        this.clientConfig = clientConfig;
        this.client = client;
        this.url = "https://controller." + clientConfig.getEnvironment() + ".pinecone.io/databases/";
    }

    public PineconeIndexOperationClient(PineconeClientConfig clientConfig) {
        this.clientConfig = clientConfig;
        client = new DefaultAsyncHttpClient();
        this.url = "https://controller." + clientConfig.getEnvironment() + ".pinecone.io/databases/";
    }

    public void deleteIndex(String indexName) throws IOException {
        System.out.println("Sending delete index request:");
        client.prepare("DELETE", url + indexName)
                .setHeader("accept", "text/plain")
                .setHeader("Api-Key", clientConfig.getApiKey())
                .execute()
                .toCompletableFuture()
                .join();

        client.close();
    }

    public void createIndex(CreateIndexRequest createIndexRequest) throws IOException {
        client.prepare("POST", url)
                .setHeader("accept", "text/plain")
                .setHeader("content-type", "application/json")
                .setHeader("Api-Key", clientConfig.getApiKey())
                .setBody(createIndexRequest.toJson())
                .execute()
                .toCompletableFuture()
                .join();

        client.close();
    }
}


