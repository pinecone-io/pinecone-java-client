package io.pinecone;

import org.asynchttpclient.*;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.*;

public class PineconeIndexOperationClientTest {

    @SuppressWarnings("unchecked")
    @Test
    public void testIndexDeletion() throws IOException {
        AsyncHttpClient mockClient = mock(DefaultAsyncHttpClient.class);
        PineconeClientConfig clientConfig = new PineconeClientConfig().withApiKey("testApiKey").withEnvironment("testEnvironment");
        Response mockResponse = mock(Response.class);
        BoundRequestBuilder mockBoundRequestBuilder = mock(BoundRequestBuilder.class);
        ListenableFuture<Response> mockResponseFuture = mock(ListenableFuture.class);
        CompletableFuture<Response> mockCompletableFuture = mock(CompletableFuture.class);

        when(mockResponse.getStatusCode()).thenReturn(202);
        when(mockClient.prepare(anyString(), anyString())).thenReturn(mockBoundRequestBuilder);
        when(mockBoundRequestBuilder.setHeader(anyString(), anyString())).thenReturn(mockBoundRequestBuilder);
        when(mockBoundRequestBuilder.execute()).thenReturn(mockResponseFuture);
        when(mockResponseFuture.toCompletableFuture()).thenReturn(mockCompletableFuture);

        PineconeIndexOperationClient indexDeletionService = new PineconeIndexOperationClient(clientConfig, mockClient);
        indexDeletionService.deleteIndex("testIndex");

        verify(mockClient, times(1)).prepare(eq("DELETE"), anyString());
        verify(mockBoundRequestBuilder).setHeader(eq("Api-Key"), eq("testApiKey"));
        verify(mockBoundRequestBuilder).execute();
        verify(mockClient).close();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testIndexCreation() throws IOException {
        AsyncHttpClient mockClient = mock(DefaultAsyncHttpClient.class);
        PineconeClientConfig clientConfig = new PineconeClientConfig().withApiKey("testApiKey").withEnvironment("testEnvironment");
        Response mockResponse = mock(Response.class);
        BoundRequestBuilder mockBoundRequestBuilder = mock(BoundRequestBuilder.class);
        ListenableFuture<Response> mockResponseFuture = mock(ListenableFuture.class);
        CompletableFuture<Response> mockCompletableFuture = mock(CompletableFuture.class);

        when(mockResponse.getStatusCode()).thenReturn(201);
        when(mockClient.prepare(anyString(), anyString())).thenReturn(mockBoundRequestBuilder);
        when(mockBoundRequestBuilder.setHeader(anyString(), anyString())).thenReturn(mockBoundRequestBuilder);
        when(mockBoundRequestBuilder.setBody(anyString())).thenReturn(mockBoundRequestBuilder);
        when(mockBoundRequestBuilder.execute()).thenReturn(mockResponseFuture);
        when(mockResponseFuture.toCompletableFuture()).thenReturn(mockCompletableFuture);

        PineconeIndexOperationClient indexDeletionService = new PineconeIndexOperationClient(clientConfig, mockClient);
        indexDeletionService.createIndex("testEnvironment", 128, "cosine");

        verify(mockClient, times(1)).prepare(eq("POST"), anyString());
        verify(mockBoundRequestBuilder).setHeader(eq("accept"), eq("text/plain"));
        verify(mockBoundRequestBuilder).setHeader(eq("content-type"), eq("application/json"));
        verify(mockBoundRequestBuilder).execute();
        verify(mockClient).close();
    }
}