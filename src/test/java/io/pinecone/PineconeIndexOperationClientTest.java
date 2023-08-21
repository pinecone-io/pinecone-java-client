package io.pinecone;

import io.pinecone.model.CreateIndexRequest;
import io.pinecone.model.IndexMetadataConfig;
import org.asynchttpclient.*;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.fail;
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
        CreateIndexRequest createIndexRequest = new CreateIndexRequest().indexName("test_name").dimension(3);
        indexDeletionService.createIndex(createIndexRequest);

        verify(mockClient, times(1)).prepare(eq("POST"), anyString());
        verify(mockBoundRequestBuilder).setHeader(eq("accept"), eq("text/plain"));
        verify(mockBoundRequestBuilder).setHeader(eq("content-type"), eq("application/json"));
        verify(mockBoundRequestBuilder).execute();
        verify(mockClient).close();
    }

    @Test
    public void testIndexCreationThrowsExceptionWithNullIndex() {
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
        CreateIndexRequest createIndexRequest = new CreateIndexRequest();

        try{
            indexDeletionService.createIndex(createIndexRequest);
            fail("Expected validation exception not occurred");
        }
        catch (PineconeValidationException validationException) {
            System.out.println("Expected PineconeValidationException with index not null occurred!");
        }
        catch (IOException e) {

        }
    }

    @Test
    public void testIndexCreationThrowsExceptionWithNullDimension() {
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
        CreateIndexRequest createIndexRequest = new CreateIndexRequest().indexName("test_name");

        try{
            indexDeletionService.createIndex(createIndexRequest);
            fail("Expected validation exception not occurred");
        }
        catch (PineconeValidationException validationException) {
            System.out.println("Expected PineconeValidationException with dimension not null occurred!");
        }
        catch (IOException e) {

        }
    }

    @Test
    public void testIndexCreationWithAllFields() {
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

        IndexMetadataConfig metadataConfig = new IndexMetadataConfig();
        List<String> indexedItems = Arrays.asList("A", "B", "C", "D");
        metadataConfig.setIndexed(indexedItems);
        PineconeIndexOperationClient indexDeletionService = new PineconeIndexOperationClient(clientConfig, mockClient);
        CreateIndexRequest createIndexRequest = new CreateIndexRequest()
                .indexName("test_name")
                .dimension(3)
                .metric("euclidean")
                .pods(2)
                .podType("p1.x2")
                .replicas(2)
                .metadataConfig(metadataConfig)
                .sourceCollection("step");

        try{
            indexDeletionService.createIndex(createIndexRequest);
        }
        catch (IOException e) {

        }
    }
}