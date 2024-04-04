package io.pinecone;

import com.google.gson.Gson;
import io.pinecone.clients.Pinecone;
import io.pinecone.exceptions.PineconeValidationException;
import okhttp3.*;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.openapitools.client.model.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class PineconeIndexOperationsTest {
    private static final Gson gson = new Gson();

    @Test
    public void testDeleteIndex() throws IOException {
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

        Pinecone client = new Pinecone.Builder("testAPiKey").withOkHttpClient(mockClient).build();
        client.deleteIndex("testIndex");

        ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);

        verify(mockClient, times(1)).newCall(requestCaptor.capture());
        verify(mockCall, times(1)).execute();
        assertEquals(requestCaptor.getValue().method(), "DELETE");
        assertEquals(requestCaptor.getValue().url().toString(), "https://api.pinecone.io/indexes/testIndex");
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
        Pinecone client = new Pinecone.Builder("testAPiKey").withOkHttpClient(mockClient).build();
        client.createIndex(createIndexRequest);

        ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);

        verify(mockClient, times(1)).newCall(requestCaptor.capture());
        verify(mockCall, times(1)).execute();
        assertEquals(requestCaptor.getValue().method(), "POST");
        assertEquals(requestCaptor.getValue().url().toString(), "https://api.pinecone.io/indexes");
    }

    @Test
    @Disabled("Re-enable when control plane validations added")
    public void testCreateIndexWithNullIndex() {
        CreateIndexRequest createIndexRequest = new CreateIndexRequest().dimension(3);

        OkHttpClient mockClient = mock(OkHttpClient.class);
        Pinecone client = new Pinecone.Builder("testAPiKey").withOkHttpClient(mockClient).build();
        client.createIndex(createIndexRequest);
        assertThrows(PineconeValidationException.class, () -> client.createIndex(createIndexRequest));
    }

    @Test
    @Disabled("Re-enable when control plane validations added")
    public void testCreateIndexWithNullDimensions() {
        CreateIndexRequest createIndexRequest = new CreateIndexRequest().name("testIndex");

        OkHttpClient mockClient = mock(OkHttpClient.class);
        Pinecone client = new Pinecone.Builder("testAPiKey").withOkHttpClient(mockClient).build();
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

        Pinecone client = new Pinecone.Builder("testAPiKey").withOkHttpClient(mockClient).build();
        IndexModel createdIndex = client.createIndex(createIndexRequest);

        ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);

        verify(mockClient, times(1)).newCall(requestCaptor.capture());
        verify(mockCall, times(1)).execute();
        assertEquals(createdIndex, expectedIndex);
        assertEquals(requestCaptor.getValue().method(), "POST");
        assertEquals(requestCaptor.getValue().url().toString(), "https://api.pinecone.io/indexes");

        // Test for null CreateIndexRequest object
        PineconeValidationException thrown = assertThrows(PineconeValidationException.class,
                () -> client.createIndex(null));
        assertEquals("CreateIndexRequest object cannot be null", thrown.getMessage());
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

        Pinecone client = new Pinecone.Builder("testAPiKey").withOkHttpClient(mockClient).build();
        IndexModel index = client.describeIndex("testIndex");

        ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);

        verify(mockClient, times(1)).newCall(requestCaptor.capture());
        verify(mockCall, times(1)).execute();
        assertEquals(expectedIndex, index);
        assertEquals(requestCaptor.getValue().method(), "GET");
        assertEquals(requestCaptor.getValue().url().toString(), "https://api.pinecone.io/indexes/testIndex");
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

        ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);

        Pinecone client = new Pinecone.Builder("testAPiKey").withOkHttpClient(mockClient).build();
        IndexList indexList = client.listIndexes();

        verify(mockClient, times(1)).newCall(requestCaptor.capture());
        verify(mockCall, times(1)).execute();
        assertEquals(indexList, expectedIndexList);
        assertEquals(requestCaptor.getValue().method(), "GET");
        assertEquals(requestCaptor.getValue().url().toString(), "https://api.pinecone.io/indexes");
    }

    @Test
    public void testConfigureIndex() throws IOException {
        String filePath = "src/test/resources/podIndexJsonString.json";
        String podIndexJsonString = new String(Files.readAllBytes(Paths.get(filePath)));
        IndexModel expectedConfiguredIndex = gson.fromJson(podIndexJsonString, IndexModel.class);

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
        Pinecone client = new Pinecone.Builder("testAPiKey").withOkHttpClient(mockClient).build();
        IndexModel configuredIndex = client.configureIndex("testPodIndex", 3);

        verify(mockCall, times(1)).execute();
        assertEquals(expectedConfiguredIndex, configuredIndex);

        // Test for empty string for index name
        PineconeValidationException thrownEmptyIndexName = assertThrows(PineconeValidationException.class,
                () -> client.configureIndex("",
                        3));
        assertEquals("indexName cannot be null or empty", thrownEmptyIndexName.getMessage());

        // Test for null as index name
        PineconeValidationException thrownNullIndexName = assertThrows(PineconeValidationException.class, () -> client.configureIndex(null,
                3));
        assertEquals("indexName cannot be null or empty", thrownNullIndexName.getMessage());

        // Test for invalid number of replicas
        PineconeValidationException thrownZeroReplicas = assertThrows(PineconeValidationException.class,
                () -> client.configureIndex("testPodIndex", 0));
        assertEquals("Number of replicas must be >= 1", thrownZeroReplicas.getMessage());
    }

    @Test
    public void testCreateCollection() throws IOException {
        String filePath = "src/test/resources/collectionCreation.json";
        String JsonStringCollection = new String(Files.readAllBytes(Paths.get(filePath)));
        CollectionModel expectedCollection = gson.fromJson(JsonStringCollection, CollectionModel.class);

        Call mockCall = mock(Call.class);
        Response mockResponse = new Response.Builder()
                .request(new Request.Builder().url("http://localhost").build())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(ResponseBody.create(JsonStringCollection, MediaType.parse("application/json")))
                .build();

        when(mockCall.execute()).thenReturn(mockResponse);

        OkHttpClient mockClient = mock(OkHttpClient.class);
        when(mockClient.newCall(any(Request.class))).thenReturn(mockCall);
        when(mockCall.execute()).thenReturn(mockResponse);

        Pinecone client = new Pinecone.Builder("testAPiKey").withOkHttpClient(mockClient).build();
        CollectionModel collection = client.createCollection(expectedCollection.getName(), "someSourceIndex");

        // Test for successful creation of Collection
        verify(mockCall, times(1)).execute();
        assertEquals(expectedCollection, collection);

        // Test for null and empty as collectionName
        PineconeValidationException thrownNullCollectionName = assertThrows(PineconeValidationException.class,
                () -> client.createCollection(null, "someSourceIndex"));
        assertEquals("collectionName cannot be null or empty", thrownNullCollectionName.getMessage());
        PineconeValidationException thrownEmptyCollectionName = assertThrows(PineconeValidationException.class,
                () -> client.createCollection("", "someSourceIndex"));
        assertEquals("collectionName cannot be null or empty", thrownEmptyCollectionName.getMessage());

        // Test for null and empty as sourceIndex
        PineconeValidationException thrownNullSourceIndex = assertThrows(PineconeValidationException.class,
                () -> client.createCollection(expectedCollection.getName(), null));
        assertEquals("sourceIndex cannot be null or empty", thrownNullSourceIndex.getMessage());
        PineconeValidationException thrownEmptySourceIndex = assertThrows(PineconeValidationException.class,
                () -> client.createCollection(expectedCollection.getName(), ""));
        assertEquals("sourceIndex cannot be null or empty", thrownEmptySourceIndex.getMessage());
    }
}
