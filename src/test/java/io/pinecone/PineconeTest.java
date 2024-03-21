package io.pinecone;

import io.pinecone.exceptions.PineconeValidationException;
import io.pinecone.clients.Pinecone;
import io.pinecone.configs.PineconeConfig;
import org.junit.jupiter.api.Disabled;
import org.openapitools.client.model.*;
import okhttp3.*;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import com.google.gson.Gson;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class PineconeTest {
    private static MockWebServer mockWebServer;
    private static Gson gson = new Gson();

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
    public void PineconeWithEmptyApiKey() {
        assertThrows(PineconeValidationException.class, () -> new Pinecone(""));
    }

    @Test
    public void PineconeConfigWithoutApiKey() {
        PineconeConfig clientConfig = new PineconeConfig(null);
        assertThrows(PineconeValidationException.class, () -> new Pinecone(clientConfig));
    }

    @Test
    public void testDeleteIndex() throws IOException {
        String indexName = "testIndex";

        Call mockCall = mock(Call.class);
        when(mockCall.execute()).thenReturn(new Response.Builder()
                .request(new Request.Builder().url("http://localhost").build())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(ResponseBody.create("Response body", MediaType.parse("text/plain")))
                .build());
        OkHttpClient mockClient = mock(OkHttpClient.class);
        when(mockClient.newCall(any(Request.class))).thenReturn(mockCall);

        Pinecone client = new Pinecone("testAPiKey", mockClient);
        client.deleteIndex(indexName);

        verify(mockClient, times(1)).newCall(any(Request.class));
        verify(mockCall, times(1)).execute();
    }

    @Test
    public void testCreatePodIndex() throws IOException {
        String filePath = "src/test/resources/podIndexJsonString.json";
        String indexJsonStringPod = new String(Files.readAllBytes(Paths.get(filePath)));

        CreateIndexRequest createIndexRequest = new CreateIndexRequest()
                .name("test_name").dimension(3).metric(IndexMetric.COSINE);

        Call mockCall = mock(Call.class);
        when(mockCall.execute()).thenReturn(new Response.Builder()
                .request(new Request.Builder().url("http://localhost").build())
                .protocol(Protocol.HTTP_1_1)
                .code(201)
                .message("OK")
                .body(ResponseBody.create(indexJsonStringPod, MediaType.parse("application/json")))
                .build());

        OkHttpClient mockClient = mock(OkHttpClient.class);
        when(mockClient.newCall(any(Request.class))).thenReturn(mockCall);
        Pinecone client = new Pinecone("testApiKey", mockClient);
        client.createIndex(createIndexRequest);

        verify(mockClient, times(1)).newCall(any(Request.class));
        verify(mockCall, times(1)).execute();
    }

    @Test
    @Disabled("Re-enable when control plane validations added")
    public void testCreateIndexWithNullIndex() {
        CreateIndexRequest createIndexRequest = new CreateIndexRequest().dimension(3);

        OkHttpClient mockClient = mock(OkHttpClient.class);
        Pinecone client = new Pinecone("testApiKey", mockClient);
        client.createIndex(createIndexRequest);
        assertThrows(PineconeValidationException.class, () -> client.createIndex(createIndexRequest));
    }

    @Test
    @Disabled("Re-enable when control plane validations added")
    public void testCreateIndexWithNullDimensions() {
        CreateIndexRequest createIndexRequest = new CreateIndexRequest().name("testIndex");

        OkHttpClient mockClient = mock(OkHttpClient.class);
        Pinecone client = new Pinecone("testApiKey", mockClient);
        client.createIndex(createIndexRequest);
        assertThrows(PineconeValidationException.class, () -> client.createIndex(createIndexRequest));
    }

    @Test
    public void testCreateIndexWithAllFields() throws IOException {
        String filePath = "src/test/resources/podIndexJsonString.json";
        String indexJsonStringPod = new String(Files.readAllBytes(Paths.get(filePath)));
        IndexModel expectedIndex = gson.fromJson(indexJsonStringPod, IndexModel.class);

        PodSpecMetadataConfig podSpecMetadataConfig = new PodSpecMetadataConfig();
        List<String> indexedItems = Arrays.asList("A", "B", "C", "D");
        podSpecMetadataConfig.setIndexed(indexedItems);

        CreateIndexRequestSpecPod requestSpecPod = new CreateIndexRequestSpecPod().pods(2).podType("p1.x2").replicas(2).metadataConfig(podSpecMetadataConfig).sourceCollection("step");
        CreateIndexRequestSpec requestSpec = new CreateIndexRequestSpec().pod(requestSpecPod);
        CreateIndexRequest createIndexRequest = new CreateIndexRequest()
                .name("test_name")
                .dimension(3)
                .metric(IndexMetric.EUCLIDEAN)
                .spec(requestSpec);

        Call mockCall = mock(Call.class);
        when(mockCall.execute()).thenReturn(new Response.Builder()
                .request(new Request.Builder().url("http://localhost").build())
                .protocol(Protocol.HTTP_1_1)
                .code(201)
                .message("OK")
                .body(ResponseBody.create(indexJsonStringPod, MediaType.parse("application/json")))
                .build());

        OkHttpClient mockClient = mock(OkHttpClient.class);
        when(mockClient.newCall(any(Request.class))).thenReturn(mockCall);

        Pinecone client = new Pinecone("testApiKey", mockClient);
        IndexModel createdIndex = client.createIndex(createIndexRequest);

        verify(mockClient, times(1)).newCall(any(Request.class));
        verify(mockCall, times(1)).execute();
        assertEquals(createdIndex, expectedIndex);
    }

    @Test
    public void testDescribeIndex() throws IOException {
        String filePath = "src/test/resources/serverlessIndexJsonString.json";
        String indexJsonStringServerless = new String(Files.readAllBytes(Paths.get(filePath)));
        IndexModel expectedIndex = gson.fromJson(indexJsonStringServerless, IndexModel.class);

        Call mockCall = mock(Call.class);
        Response mockResponse = new Response.Builder()
                .request(new Request.Builder().url("http://localhost").build())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(ResponseBody.create(indexJsonStringServerless, MediaType.parse("application/json")))
                .build();

        when(mockCall.execute()).thenReturn(mockResponse);

        OkHttpClient mockClient = mock(OkHttpClient.class);
        when(mockClient.newCall(any(Request.class))).thenReturn(mockCall);
        when(mockCall.execute()).thenReturn(mockResponse);

        Pinecone client = new Pinecone("testApiKey", mockClient);
        IndexModel index = client.describeIndex("testIndex");

        assertEquals(expectedIndex, index);
    }

    @Test
    public void testListIndexes() throws IOException {
        String filePath = "src/test/resources/indexListJsonString.json";
        String indexListJsonString = new String(Files.readAllBytes(Paths.get(filePath)));
        IndexList expectedIndexList = gson.fromJson(indexListJsonString, IndexList.class);

        Call mockCall = mock(Call.class);

        Response mockResponse = new Response.Builder()
                .request(new Request.Builder().url("http://localhost").build())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(ResponseBody.create(indexListJsonString, MediaType.parse("application/json")))
                .build();

        OkHttpClient mockClient = mock(OkHttpClient.class);
        when(mockClient.newCall(any(Request.class))).thenReturn(mockCall);
        when(mockCall.execute()).thenReturn(mockResponse);

        Pinecone client = new Pinecone("testApiKey", mockClient);
        IndexList indexList = client.listIndexes();
        assertEquals(indexList, expectedIndexList);
    }

    @Test
    public void testConfigureIndex() throws IOException {
        String filePath = "src/test/resources/podIndexJsonString.json";
        String podIndexJsonString = new String(Files.readAllBytes(Paths.get(filePath)));
        IndexModel expectedConfiguredIndex = gson.fromJson(podIndexJsonString, IndexModel.class);

        ConfigureIndexRequestSpecPod pod = new ConfigureIndexRequestSpecPod().podType("s1.x2").replicas(3);
        ConfigureIndexRequestSpec spec = new ConfigureIndexRequestSpec().pod(pod);
        ConfigureIndexRequest configureIndexRequest = new ConfigureIndexRequest().spec(spec);

        Call mockCall = mock(Call.class);
        when(mockCall.execute()).thenReturn(new Response.Builder()
                .request(new Request.Builder().url("http://localhost").build())
                .protocol(Protocol.HTTP_1_1)
                .code(202)
                .message("The index has been successfully updated.")
                .body(ResponseBody.create(podIndexJsonString, MediaType.parse("application/json")))
                .build());

        OkHttpClient mockClient = mock(OkHttpClient.class);
        when(mockClient.newCall(any(Request.class))).thenReturn(mockCall);
        Pinecone client = new Pinecone("testApiKey", mockClient);
        IndexModel configuredIndex = client.configureIndex("testIndex", configureIndexRequest);

        verify(mockClient, times(1)).newCall(any(Request.class));
        verify(mockCall, times(1)).execute();
        assertEquals(expectedConfiguredIndex, configuredIndex);
    }
}