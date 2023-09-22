package io.pinecone;

import io.pinecone.exceptions.PineconeConfigurationException;
import io.pinecone.exceptions.PineconeValidationException;
import io.pinecone.model.CreateIndexRequest;
import io.pinecone.model.IndexMetadataConfig;
import okhttp3.*;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class PineconeIndexOperationClientTest {
    private static MockWebServer mockWebServer;

    @BeforeAll
    public static void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @AfterAll
    public static void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    public void IndexOpsWithoutApiKey() throws IOException {
        PineconeClientConfig clientConfig = new PineconeClientConfig()
                .withEnvironment("testEnvironment");
        OkHttpClient mockClient = mock(OkHttpClient.class);

        assertThrows(PineconeConfigurationException.class, () -> new PineconeIndexOperationClient(clientConfig, mockClient));
    }

    @Test
    public void IndexOpsWithoutEnvironment() throws IOException {
        PineconeClientConfig clientConfig = new PineconeClientConfig()
                .withApiKey("testApiKey");
        OkHttpClient mockClient = mock(OkHttpClient.class);

        assertThrows(PineconeConfigurationException.class, () -> new PineconeIndexOperationClient(clientConfig, mockClient));
    }

    @Test
    public void IndexOpsWithoutApiKeyAndEnvironment() throws IOException {
        PineconeClientConfig clientConfig = new PineconeClientConfig();
        OkHttpClient mockClient = mock(OkHttpClient.class);

        assertThrows(PineconeConfigurationException.class, () -> new PineconeIndexOperationClient(clientConfig, mockClient));
    }

    @Test
    public void testDeleteIndex() throws IOException {
        String indexName = "testIndex";
        PineconeClientConfig clientConfig = new PineconeClientConfig()
                .withApiKey("testApiKey")
                .withEnvironment("testEnvironment");

        Call mockCall = mock(Call.class);
        when(mockCall.execute()).thenReturn(new Response.Builder()
                .request(new Request.Builder().url("http://localhost").build())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("The index has been successfully deleted.")
                .body(ResponseBody.create(MediaType.parse("text/plain"), "Response body"))
                .build());

        OkHttpClient mockClient = mock(OkHttpClient.class);
        when(mockClient.newCall(any(Request.class))).thenReturn(mockCall);
        PineconeIndexOperationClient indexOperationClient = new PineconeIndexOperationClient(clientConfig, mockClient);
        indexOperationClient.deleteIndex(indexName);

        verify(mockClient, times(1)).newCall(any(Request.class));
        verify(mockCall, times(1)).execute();
    }

    @Test
    public void testCreateIndex() throws IOException {
        PineconeClientConfig clientConfig = new PineconeClientConfig()
                .withApiKey("testApiKey")
                .withEnvironment("testEnvironment");
        CreateIndexRequest createIndexRequest = new CreateIndexRequest()
                .withIndexName("test_name").withDimension(3);

        Call mockCall = mock(Call.class);
        when(mockCall.execute()).thenReturn(new Response.Builder()
                .request(new Request.Builder().url("http://localhost").build())
                .protocol(Protocol.HTTP_1_1)
                .code(201)
                .message("The index has been successfully created")
                .body(ResponseBody.create(MediaType.parse("text/plain"), "Response body"))
                .build());

        OkHttpClient mockClient = mock(OkHttpClient.class);
        when(mockClient.newCall(any(Request.class))).thenReturn(mockCall);
        PineconeIndexOperationClient indexOperationClient = new PineconeIndexOperationClient(clientConfig, mockClient);
        indexOperationClient.createIndex(createIndexRequest);

        verify(mockClient, times(1)).newCall(any(Request.class));
        verify(mockCall, times(1)).execute();
    }

    @Test
    public void testCreateIndexWithNullIndex() {
        PineconeClientConfig clientConfig = new PineconeClientConfig()
                .withApiKey("testApiKey")
                .withEnvironment("testEnvironment");
        CreateIndexRequest createIndexRequest = new CreateIndexRequest().withDimension(3);

        OkHttpClient mockClient = mock(OkHttpClient.class);
        PineconeIndexOperationClient indexOperationClient = new PineconeIndexOperationClient(clientConfig, mockClient);
        assertThrows(PineconeValidationException.class, () -> indexOperationClient.createIndex(createIndexRequest));
    }

    @Test
    public void testCreateIndexWithNullDimensions() {
        PineconeClientConfig clientConfig = new PineconeClientConfig()
                .withApiKey("testApiKey")
                .withEnvironment("testEnvironment");
        CreateIndexRequest createIndexRequest = new CreateIndexRequest().withIndexName("testIndexName");

        OkHttpClient mockClient = mock(OkHttpClient.class);
        PineconeIndexOperationClient indexOperationClient = new PineconeIndexOperationClient(clientConfig, mockClient);
        assertThrows(PineconeValidationException.class, () -> indexOperationClient.createIndex(createIndexRequest));
    }

    @Test
    public void testCreateIndexWithAllFields() throws IOException {
        PineconeClientConfig clientConfig = new PineconeClientConfig()
                .withApiKey("testApiKey")
                .withEnvironment("testEnvironment");

        IndexMetadataConfig metadataConfig = new IndexMetadataConfig();
        List<String> indexedItems = Arrays.asList("A", "B", "C", "D");
        metadataConfig.setIndexed(indexedItems);

        CreateIndexRequest createIndexRequest = new CreateIndexRequest()
                .withIndexName("test_name")
                .withDimension(3)
                .withMetric("euclidean")
                .withPods(2)
                .withPodType("p1.x2")
                .withReplicas(2)
                .withMetadataConfig(metadataConfig)
                .withSourceCollection("step");

        Call mockCall = mock(Call.class);
        when(mockCall.execute()).thenReturn(new Response.Builder()
                .request(new Request.Builder().url("http://localhost").build())
                .protocol(Protocol.HTTP_1_1)
                .code(201)
                .message("The index has been successfully created")
                .body(ResponseBody.create(MediaType.parse("text/plain"), "Response body"))
                .build());

        OkHttpClient mockClient = mock(OkHttpClient.class);
        when(mockClient.newCall(any(Request.class))).thenReturn(mockCall);

        PineconeIndexOperationClient indexOperationClient = new PineconeIndexOperationClient(clientConfig, mockClient);
        indexOperationClient.createIndex(createIndexRequest);

        verify(mockClient, times(1)).newCall(any(Request.class));
        verify(mockCall, times(1)).execute();
    }
}