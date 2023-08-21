package io.pinecone;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClient;

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

    public void createIndex(String indexName, int dimension, String metric) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.createObjectNode()
                .put("metric", metric)
                .put("pods", 1)
                .put("replicas", 1)
                .put("pod_type", "p1.x1")
                .put("name", indexName)
                .put("dimension", dimension);

        client.prepare("POST", url)
                .setHeader("accept", "text/plain")
                .setHeader("content-type", "application/json")
                .setHeader("Api-Key", clientConfig.getApiKey())
                .setBody(jsonNode.toString())
                .execute()
                .toCompletableFuture()
                .join();

        client.close();
    }
}


