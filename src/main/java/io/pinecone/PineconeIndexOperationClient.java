package io.pinecone;

import org.asynchttpclient.AsyncHttpClient;

import java.io.IOException;

public class PineconeIndexOperationClient {
    private AsyncHttpClient client;
    private PineconeClientConfig clientConfig;
    private String url;

    // for testing purpose only
    PineconeIndexOperationClient(PineconeClientConfig clientConfig, AsyncHttpClient client) {
        this.clientConfig = clientConfig;
        this.client = client;
        this.url = "https://controller." + clientConfig.getEnvironment() + ".pinecone.io/databases/";
    }

    public PineconeIndexOperationClient(PineconeClientConfig clientConfig) {
        this.clientConfig = clientConfig;
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
}


