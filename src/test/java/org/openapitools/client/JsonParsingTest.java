package org.openapitools.client;

import okhttp3.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openapitools.db_control.client.api.ManageIndexesApi;
import org.openapitools.db_control.client.model.*;
import org.openapitools.db_control.client.ApiClient;
import org.openapitools.db_control.client.Configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class JsonParsingTest {
    private OkHttpClient okHttpClient;
    private ManageIndexesApi api;

    @BeforeEach
    public void setup() {
        okHttpClient = mock(OkHttpClient.class);
        ApiClient apiClient = new ApiClient(okHttpClient);
        api = new ManageIndexesApi(apiClient);
    }

    private void setupMockResponse(String jsonResponse) throws Exception {
        Call call = mock(Call.class);
        Response response = new Response.Builder()
                .request(new Request.Builder().url("http://example.com").build())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("")
                .body(ResponseBody.create(
                        jsonResponse, // JSON response
                        MediaType.get("application/json; charset=utf-8")
                ))
                .build();

        when(call.execute()).thenReturn(response);
        when(okHttpClient.newCall(any(Request.class))).thenReturn(call);
    }

    private String readJsonFromFile(String path) throws IOException {
        // Ensure the path is correctly referenced from your resources directory
        return new String(Files.readAllBytes(Paths.get(path)));
    }

    @Test
    public void test_describeIndex_happyPath() throws Exception {
        String jsonResponse = readJsonFromFile("src/test/resources/describeIndexResponse.valid.json");
        setupMockResponse(jsonResponse);

        IndexModel indexModel = api.describeIndex(Configuration.VERSION, "test-index");

        // Don't need a ton of assertions here. The point is the code didn't blow up
        // due to parsing the JSON response.
        assertEquals("test-index", indexModel.getName());
        assertEquals("Ready", indexModel.getStatus().getState());
    }

    @Test
    public void test_describeIndex_extraProperties() throws Exception {
        String jsonResponse = readJsonFromFile("src/test/resources/describeIndexResponse.withUnknownProperties.json");
        setupMockResponse(jsonResponse);

        IndexModel indexModel = api.describeIndex(Configuration.VERSION, "test-index");

        assertEquals("test-index", indexModel.getName());
        assertEquals("Ready", indexModel.getStatus().getState());
    }

    @Test
    public void test_createIndex_happyPath() throws Exception {
        String jsonResponse = readJsonFromFile("src/test/resources/createIndexResponse.valid.json");
        setupMockResponse(jsonResponse);

        CreateIndexRequest createIndexRequest = new CreateIndexRequest();
        createIndexRequest.setName("test-index");
        createIndexRequest.setDimension(1536);
        createIndexRequest.setMetric("cosine");
        createIndexRequest.setSpec(new IndexSpec());
        IndexModel indexModel = api.createIndex(Configuration.VERSION, createIndexRequest);

        assertEquals("serverless-index", indexModel.getName());
        assertEquals("Ready", indexModel.getStatus().getState());
    }

    @Test
    public void test_createIndex_extraProperties() throws Exception {
        String jsonResponse = readJsonFromFile("src/test/resources/createIndexResponse.withUnknownProperties.json");
        setupMockResponse(jsonResponse);

        CreateIndexRequest createIndexRequest = new CreateIndexRequest();
        createIndexRequest.setName("test-index");
        createIndexRequest.setDimension(1536);
        createIndexRequest.setMetric("cosine");
        createIndexRequest.setSpec(new IndexSpec());
        IndexModel indexModel = api.createIndex(Configuration.VERSION, createIndexRequest);

        assertEquals("serverless-index", indexModel.getName());
        assertEquals("Ready", indexModel.getStatus().getState());
    }

    @Test
    public void test_describeCollection_happyPath() throws Exception {
        String jsonResponse = readJsonFromFile("src/test/resources/describeCollection.valid.json");
        setupMockResponse(jsonResponse);

        CollectionModel collectionModel = api.describeCollection(Configuration.VERSION, "tiny-collection");

        assertEquals("tiny-collection", collectionModel.getName());
        assertEquals("Ready", collectionModel.getStatus());
    }
}
