package io.pinecone;

import org.asynchttpclient.*;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.*;

public class PineconeIndexOperationClientTest {

    @SuppressWarnings("unchecked")
    @Test
    public void testSuccessfulIndexDeletion() throws IOException {
        AsyncHttpClient mockClient = mock(DefaultAsyncHttpClient.class);
        PineconeClientConfig clientConfig = new PineconeClientConfig().withApiKey("testApiKey").withEnvironment("testEnvironment");
        PineconeConnectionConfig connectionConfig = new PineconeConnectionConfig().withIndexName("testIndex");
        Response mockResponse = mock(Response.class);
        BoundRequestBuilder mockBoundRequestBuilder = mock(BoundRequestBuilder.class);
        ListenableFuture<Response> mockResponseFuture = mock(ListenableFuture.class);
        CompletableFuture<Response> mockCompletableFuture = mock(CompletableFuture.class);

        when(mockResponse.getStatusCode()).thenReturn(202);
        when(mockClient.prepare(anyString(), anyString())).thenReturn(mockBoundRequestBuilder);
        when(mockBoundRequestBuilder.setHeader(anyString(), anyString())).thenReturn(mockBoundRequestBuilder);
        when(mockBoundRequestBuilder.setHeader(eq("Api-Key"), eq("testApiKey"))).thenReturn(mockBoundRequestBuilder);
        when(mockBoundRequestBuilder.execute()).thenReturn(mockResponseFuture);
        when(mockResponseFuture.toCompletableFuture()).thenReturn(mockCompletableFuture);

        PineconeIndexOperationClient indexDeletionService = new PineconeIndexOperationClient(clientConfig, connectionConfig, mockClient);
        indexDeletionService.deleteIndex();

        verify(mockClient, times(1)).prepare(eq("DELETE"), anyString());
        verify(mockBoundRequestBuilder).setHeader(eq("Api-Key"), eq("testApiKey"));
        verify(mockBoundRequestBuilder).execute();
        verify(mockClient).close();
    }
}