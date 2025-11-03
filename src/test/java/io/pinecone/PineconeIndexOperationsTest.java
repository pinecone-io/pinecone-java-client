package io.pinecone;

import io.pinecone.clients.Pinecone;
import io.pinecone.exceptions.PineconeValidationException;
import okhttp3.*;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.openapitools.db_control.client.model.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class PineconeIndexOperationsTest {
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
    public void testCreatePodsIndex() throws IOException {
        String filePath = "src/test/resources/podIndexJsonString.json";
        String indexJsonStringPod = new String(Files.readAllBytes(Paths.get(filePath)));

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

        String indexName = "testPodIndex";
        client.createPodsIndex(indexName,
                3,
                "us-east-1-aws",
                "p1.x1",
                "cosine",
                2,
                1,
                2,
                new PodSpecMetadataConfig(),
                "some-source-collection",
                "disabled",
                null);

        ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);

        verify(mockClient, times(1)).newCall(requestCaptor.capture());
        verify(mockCall, times(1)).execute();
        assertEquals(requestCaptor.getValue().method(), "POST");
        assertEquals(requestCaptor.getValue().url().toString(), "https://api.pinecone.io/indexes");
    }

    @Test
    public void testValidatePodIndexParams() {
        // indexName
        PineconeValidationException thrownEmptyIndexName = assertThrows(PineconeValidationException.class,
                () -> Pinecone.validatePodIndexParams("", 3, "some-environment", "p1.x1", "cosine", null,
                        null,
                        null));
        assertEquals("indexName cannot be null or empty", thrownEmptyIndexName.getMessage());

        PineconeValidationException thrownNullIndexName = assertThrows(PineconeValidationException.class,
                () -> Pinecone.validatePodIndexParams(null, 3, "some-environment", "p1.x1", "cosine", null,
                        null,
                        null));
        assertEquals("indexName cannot be null or empty", thrownNullIndexName.getMessage());

        // Dimension
        PineconeValidationException thrownNegativeDimension = assertThrows(PineconeValidationException.class,
                () -> Pinecone.validatePodIndexParams("test-index", -3, "some-environment", "p1.x1", "cosine", null,
                        null,
                        null));
        assertEquals("Dimension must be greater than 0. See limits for more info: https://docs.pinecone.io/reference/limits", thrownNegativeDimension.getMessage());

        PineconeValidationException thrownNullDimension = assertThrows(PineconeValidationException.class,
                () -> Pinecone.validatePodIndexParams("test-index", null, "some-environment", "p1.x1", "cosine", null,
                        null,
                        null));
        assertEquals("Dimension cannot be null", thrownNullDimension.getMessage());

        // Environment
        PineconeValidationException thrownEmptyEnvironment = assertThrows(PineconeValidationException.class,
                () -> Pinecone.validatePodIndexParams("test-index", 3, "", "p1.x1", "cosine", null,
                        null,
                        null));
        assertEquals("Environment cannot be null or empty", thrownEmptyEnvironment.getMessage());

        PineconeValidationException thrownNullEnvironment = assertThrows(PineconeValidationException.class,
                () -> Pinecone.validatePodIndexParams("test-index", 3, null, "p1.x1", "cosine", null,
                        null,
                        null));
        assertEquals("Environment cannot be null or empty", thrownNullEnvironment.getMessage());

        // podType
        PineconeValidationException thrownNullPodType = assertThrows(PineconeValidationException.class,
                () -> Pinecone.validatePodIndexParams("test-index", 3, "some-environment", null, "cosine", null,
                        null,
                        null));
        assertEquals("podType cannot be null or empty", thrownNullPodType.getMessage());

        PineconeValidationException thrownEmptyPodType = assertThrows(PineconeValidationException.class,
                () -> Pinecone.validatePodIndexParams("test-index", 3, "some-environment", "", "cosine", null,
                        null,
                        null));
        assertEquals("podType cannot be null or empty", thrownEmptyPodType.getMessage());

        // Metric
        PineconeValidationException thrownEmptyMetric = assertThrows(PineconeValidationException.class,
                () -> Pinecone.validatePodIndexParams("test-index", 3, "some-environment", "p1.x1", "", null,
                        null,
                        null));
        assertEquals("Metric cannot be null or empty. Must be cosine, euclidean, or dotproduct.", thrownEmptyMetric.getMessage());

        // Replicas
        PineconeValidationException thrownNegativeReplicas = assertThrows(PineconeValidationException.class,
                () -> Pinecone.validatePodIndexParams("test-index",
                        3,
                        "some-environment",
                        "p1.x1",
                        "cosine",
                        -1,
                        2,
                        -2));
        assertEquals("Number of replicas must be >= 1", thrownNegativeReplicas.getMessage());

        // Shards
        PineconeValidationException thrownNegativeShards = assertThrows(PineconeValidationException.class,
                () -> Pinecone.validatePodIndexParams("test-index",
                        3,
                        "some-environment",
                        "p1.x1",
                        "cosine",
                        1,
                        -1,
                        -1));
        assertEquals("Number of shards must be >= 1", thrownNegativeShards.getMessage());

        // Shards*replicas = pods
        PineconeValidationException incorrectNumReplicasAndShards = assertThrows(PineconeValidationException.class,
                () -> Pinecone.validatePodIndexParams("test-index", 3, "some-environment", "cosine", "p1.x1", 3, 2, 9));
        assertEquals("Number of pods does not equal number of shards times number of replicas", incorrectNumReplicasAndShards.getMessage());
    }

    @Test
    public void testDescribeIndex() throws IOException {
        String filePath = "src/test/resources/serverlessIndexJsonString.json";
        String indexJsonStringServerless = new String(Files.readAllBytes(Paths.get(filePath)));

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
        // Create expected index after client creation to ensure JSON class is initialized
        IndexModel expectedIndex = IndexModel.fromJson(indexJsonStringServerless);
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
        IndexList expectedIndexList = IndexList.fromJson(indexListJsonString);

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
        IndexModel expectedConfiguredIndex = IndexModel.fromJson(podIndexJsonString);

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
        IndexModel configuredIndex = client.configurePodsIndex("testPodIndex", 3, "disabled");

        verify(mockCall, times(1)).execute();
        assertEquals(expectedConfiguredIndex, configuredIndex);

        // Test for empty string for index name
        PineconeValidationException thrownEmptyIndexName = assertThrows(PineconeValidationException.class,
                () -> client.configurePodsIndex("",
                        3, "disabled"));
        assertEquals("indexName cannot be null or empty", thrownEmptyIndexName.getMessage());

        // Test for null as index name
        PineconeValidationException thrownNullIndexName = assertThrows(PineconeValidationException.class, () -> client.configurePodsIndex(null,
                3, "disabled"));
        assertEquals("indexName cannot be null or empty", thrownNullIndexName.getMessage());

        // Test for invalid number of replicas
        PineconeValidationException thrownZeroReplicas = assertThrows(PineconeValidationException.class,
                () -> client.configurePodsIndex("testPodIndex", 0, "disabled"));
        assertEquals("Number of replicas must be >= 1", thrownZeroReplicas.getMessage());
    }

    @Test
    public void testCreateCollection() throws IOException {
        String filePath = "src/test/resources/collectionCreation.json";
        String JsonStringCollection = new String(Files.readAllBytes(Paths.get(filePath)));
        CollectionModel expectedCollection = CollectionModel.fromJson(JsonStringCollection);

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
