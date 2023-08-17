package io.pinecone;

import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClient;

import java.io.IOException;

public class PineconeIndexOperationClient {

    private AsyncHttpClient client;

    PineconeIndexOperationClient(AsyncHttpClient client) {
        this.client = client;
    }

    public void deleteIndex(String environment, String apiKey, String indexName) throws IOException {
        String url = "https://controller." + environment + ".pinecone.io/databases/" + indexName;
        System.out.println("Sending delete index request:");
        // ToDo: Capture Response response and customized error messages
        client.prepare("DELETE", url)
                .setHeader("accept", "text/plain")
                .setHeader("Api-Key", apiKey)
                .execute()
                .toCompletableFuture()
                .join();

        client.close();
    }
}


