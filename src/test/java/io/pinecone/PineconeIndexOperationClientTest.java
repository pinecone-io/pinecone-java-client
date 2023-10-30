package io.pinecone;

import io.pinecone.exceptions.PineconeConfigurationException;
import io.pinecone.exceptions.PineconeValidationException;
import io.pinecone.model.*;
import okhttp3.*;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
    public void IndexOpsWithoutApiKey() {
        PineconeClientConfig clientConfig = new PineconeClientConfig()
                .withEnvironment("testEnvironment");
        OkHttpClient mockClient = mock(OkHttpClient.class);

        assertThrows(PineconeConfigurationException.class, () -> new PineconeIndexOperationClient(clientConfig, mockClient));
    }

    @Test
    public void IndexOpsWithoutEnvironment() {
        PineconeClientConfig clientConfig = new PineconeClientConfig()
                .withApiKey("testApiKey");
        OkHttpClient mockClient = mock(OkHttpClient.class);

        assertThrows(PineconeConfigurationException.class, () -> new PineconeIndexOperationClient(clientConfig, mockClient));
    }

    @Test
    public void IndexOpsWithoutApiKeyAndEnvironment() {
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
                .body(ResponseBody.create("Response body", MediaType.parse("text/plain")))
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
                .body(ResponseBody.create("Response body", MediaType.parse("text/plain")))
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
                .body(ResponseBody.create("Response body", MediaType.parse("text/plain")))
                .build());

        OkHttpClient mockClient = mock(OkHttpClient.class);
        when(mockClient.newCall(any(Request.class))).thenReturn(mockCall);

        PineconeIndexOperationClient indexOperationClient = new PineconeIndexOperationClient(clientConfig, mockClient);
        indexOperationClient.createIndex(createIndexRequest);

        verify(mockClient, times(1)).newCall(any(Request.class));
        verify(mockCall, times(1)).execute();
    }

    @Test
    public void testDescribeIndex() throws IOException {
        String indexName = "testIndex";
        String indexJsonString = "{\"database\":{\"name\":\"testIndex\",\"metric\":\"cosine\",\"dimension\":3," +
                "\"replicas\":1,\"shards\":1,\"pods\":1,\"pod_type\":\"p1.x1\"}," +
                "\"status\":{\"waiting\":[],\"crashed\":[],\"host\":\"url\",\"port\":433," +
                "\"state\":\"Ready\",\"ready\":true}}";
        PineconeClientConfig clientConfig = new PineconeClientConfig()
                .withApiKey("testApiKey")
                .withEnvironment("testEnvironment");
        IndexMetaDatabase metaDatabase = new IndexMetaDatabase()
                .withName("testIndex")
                .withMetric("cosine")
                .withDimension(3)
                .withReplicas(1)
                .withShards(1)
                .withPods(1)
                .withPodType("p1.x1");
        IndexStatus indexStatus = new IndexStatus()
                .withHost("url")
                .withState("Ready")
                .withReady(true)
                .withPort("433");

        IndexMeta expectedIndexMeta = new IndexMeta()
                .withMetaDatabase(metaDatabase)
                .withStatus(indexStatus);

        Call mockCall = mock(Call.class);
        Response mockResponse = new Response.Builder()
                .request(new Request.Builder().url("http://localhost").build())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(ResponseBody.create(indexJsonString, MediaType.parse("application/json")))
                .build();

        when(mockCall.execute()).thenReturn(mockResponse);

        OkHttpClient mockClient = mock(OkHttpClient.class);
        when(mockClient.newCall(any(Request.class))).thenReturn(mockCall);
        when(mockCall.execute()).thenReturn(mockResponse);

        PineconeIndexOperationClient indexOperationClient = new PineconeIndexOperationClient(clientConfig, mockClient);
        IndexMeta actualIndexMeta = indexOperationClient.describeIndex(indexName);

        assertEquals(expectedIndexMeta.toString(), actualIndexMeta.toString());
    }

    @Test
    public void testListIndexes() throws IOException {
        String responseBody = "[\"index1\",\"index2\"]";
        PineconeClientConfig clientConfig = new PineconeClientConfig()
                .withApiKey("api_key")
                .withEnvironment("testEnvironment");

        Call mockCall = mock(Call.class);

        Response mockResponse = new Response.Builder()
                .request(new Request.Builder().url("http://localhost").build())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("The index has been successfully deleted.")
                .body(ResponseBody.create(responseBody, MediaType.parse("text/plain")))
                .build();

        OkHttpClient mockClient = mock(OkHttpClient.class);
        when(mockClient.newCall(any(Request.class))).thenReturn(mockCall);
        when(mockCall.execute()).thenReturn(mockResponse);

        PineconeIndexOperationClient indexOperationClient = new PineconeIndexOperationClient(clientConfig, mockClient);
        List<String> indexList = indexOperationClient.listIndexes();
        assertEquals(2, indexList.size());
        assertEquals(indexList.get(0), "index1");
        assertEquals(indexList.get(1), "index2");

        // Empty response
        String emptyResponseBody = "[]";
        Response mockEmptyResponse = new Response.Builder()
                .request(new Request.Builder().url("http://localhost").build())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("Empty response.")
                .body(ResponseBody.create(emptyResponseBody, MediaType.parse("text/plain")))
                .build();

        when(mockCall.execute()).thenReturn(mockEmptyResponse);

        List<String> emptyIndexList = indexOperationClient.listIndexes();
        assertNotNull(emptyIndexList);
        assertTrue(emptyIndexList.isEmpty());
    }

    @Test
    public void testConfigureIndex() throws IOException {
        String indexName = "test-index";
        PineconeClientConfig clientConfig = new PineconeClientConfig()
                .withApiKey("testApiKey")
                .withEnvironment("testEnvironment");
        ConfigureIndexRequest configureIndexRequest = new ConfigureIndexRequest()
                .withPodType("s1.x2").withReplicas(3);

        Call mockCall = mock(Call.class);
        when(mockCall.execute()).thenReturn(new Response.Builder()
                .request(new Request.Builder().url("http://localhost").build())
                .protocol(Protocol.HTTP_1_1)
                .code(202)
                .message("The index has been successfully updated.")
                .body(ResponseBody.create("Response body", MediaType.parse("text/plain")))
                .build());

        OkHttpClient mockClient = mock(OkHttpClient.class);
        when(mockClient.newCall(any(Request.class))).thenReturn(mockCall);
        PineconeIndexOperationClient indexOperationClient = new PineconeIndexOperationClient(clientConfig, mockClient);
        indexOperationClient.configureIndex(indexName, configureIndexRequest);

        verify(mockClient, times(1)).newCall(any(Request.class));
        verify(mockCall, times(1)).execute();
    }
}