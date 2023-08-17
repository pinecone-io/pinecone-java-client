package io.pinecone;

import org.asynchttpclient.AsyncHttpClient;

import java.io.IOException;

public class PineconeIndexOperationClient {
    private AsyncHttpClient client;
    private PineconeClientConfig clientConfig;
    private PineconeConnectionConfig connectionConfig;
    private String url;

    PineconeIndexOperationClient(PineconeClientConfig clientConfig, PineconeConnectionConfig connectionConfig, AsyncHttpClient client) {
        this.clientConfig = clientConfig;
        this.connectionConfig = connectionConfig;
        this.client = client;
        this.url = "https://controller." + clientConfig.getEnvironment() + ".pinecone.io/databases/";
    }

    public PineconeIndexOperationClient(PineconeClientConfig clientConfig, PineconeConnectionConfig connectionConfig) {
        this.clientConfig = clientConfig;
        this.connectionConfig = connectionConfig;
        this.url = "https://controller." + clientConfig.getEnvironment() + ".pinecone.io/databases/";
    }

    public void deleteIndex() throws IOException {
        System.out.println("Sending delete index request:");
        // ToDo: Capture Response response and customized error messages
        client.prepare("DELETE", url + connectionConfig.getIndexName())
                .setHeader("accept", "text/plain")
                .setHeader("Api-Key", clientConfig.getApiKey())
                .execute()
                .toCompletableFuture()
                .join();

        client.close();
    }
}


