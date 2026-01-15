package io.pinecone;

import io.pinecone.exceptions.PineconeConfigurationException;
import io.pinecone.clients.Pinecone;
import org.mockito.ArgumentCaptor;
import org.openapitools.db_control.client.model.*;
import okhttp3.*;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.AbstractMap;

import static io.pinecone.commons.Constants.pineconeClientVersion;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PineconeBuilderTest {

    private static AbstractMap.SimpleEntry<Call, OkHttpClient> buildMockCallAndClient(ResponseBody response) throws IOException {
        Response mockResponse = new Response.Builder()
                .request(new Request.Builder().url("http://localhost").build())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(response)
                .build();

        Call mockCall = mock(Call.class);
        when(mockCall.execute()).thenReturn(mockResponse);

        OkHttpClient mockClient = mock(OkHttpClient.class);
        when(mockClient.newCall(any(Request.class))).thenReturn(mockCall);

        return new AbstractMap.SimpleEntry<>(mockCall, mockClient);
    }

    @Test
    public void PineconeWithNullApiKey() {
        PineconeConfigurationException exception = assertThrows(PineconeConfigurationException.class, () -> {
            new Pinecone.Builder(null).build();
        });
        assertTrue(exception.getLocalizedMessage().contains("The API key is required and must not be empty or null"));
    }

    @Test
    public void PineconeWithEmptyApiKey() {
        PineconeConfigurationException exception = assertThrows(PineconeConfigurationException.class, () -> {
            new Pinecone.Builder("").build();
        });
        assertTrue(exception.getLocalizedMessage().contains("The API key is required and must not be empty or null"));
    }

    @Test
    public void PineconeWithOkHttpClientAndUserAgent() throws IOException {
        String filePath = "src/test/resources/serverlessIndexJsonString.json";
        String indexJsonStringServerless = new String(Files.readAllBytes(Paths.get(filePath)));

        AbstractMap.SimpleEntry<Call, OkHttpClient> mockCallAndClient =
                buildMockCallAndClient(ResponseBody.create(
                        indexJsonStringServerless,
                        MediaType.parse("application/json")));

        OkHttpClient mockClient = mockCallAndClient.getValue();

        Pinecone client = new Pinecone.Builder("testAPiKey")
                .withOkHttpClient(mockClient)
                .build();
        // Create expected index after client creation to ensure JSON class is initialized
        IndexModel expectedIndex = IndexModel.fromJson(indexJsonStringServerless);
        IndexModel index = client.describeIndex("testIndex");

        ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);

        assertEquals(expectedIndex, index);
        verify(mockClient, times(1)).newCall(requestCaptor.capture());
        assertEquals("lang=java; pineconeClientVersion=" + pineconeClientVersion, requestCaptor.getValue().header("User-Agent"));
    }

    @Test
    public void PineconeWithSourceTag()  throws IOException {
        String filePath = "src/test/resources/serverlessIndexJsonString.json";
        String indexJsonStringServerless = new String(Files.readAllBytes(Paths.get(filePath)));

        AbstractMap.SimpleEntry<Call, OkHttpClient> mockCallAndClient =
                buildMockCallAndClient(ResponseBody.create(
                        indexJsonStringServerless,
                        MediaType.parse("application/json")));

        OkHttpClient mockClient = mockCallAndClient.getValue();

        Pinecone client = new Pinecone.Builder("testAPiKey")
                .withSourceTag("testSourceTag")
                .withOkHttpClient(mockClient)
                .build();
        // Create expected index after client creation to ensure JSON class is initialized
        IndexModel expectedIndex = IndexModel.fromJson(indexJsonStringServerless);
        IndexModel index = client.describeIndex("testIndex");

        ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);

        assertEquals(expectedIndex, index);
        verify(mockClient, times(1)).newCall(requestCaptor.capture());
        assertEquals("lang=java; pineconeClientVersion=" + pineconeClientVersion + "; source_tag=testSourceTag", requestCaptor.getValue().header("User-Agent"));
    }

    @Test
    public void PineconeWithHostAndCustomOkHttpClient() {
        String customHost = "http://localhost:5080";
        OkHttpClient customClient = new OkHttpClient.Builder().build();

        // Verify that the builder doesn't throw an exception when both host and custom client are set
        Pinecone client = new Pinecone.Builder("testApiKey")
                .withHost(customHost)
                .withOkHttpClient(customClient)
                .build();

        assertNotNull(client, "Pinecone client should be created successfully with both host and custom OkHttpClient");
    }

    @Test
    public void PineconeWithHostButNoCustomOkHttpClient() {
        String customHost = "http://localhost:5080";

        // Verify that the builder doesn't throw an exception when host is set without custom client
        Pinecone client = new Pinecone.Builder("testApiKey")
                .withHost(customHost)
                .build();

        assertNotNull(client, "Pinecone client should be created successfully with host but no custom OkHttpClient");
    }

    @Test
    public void PineconeWithCustomOkHttpClientButNoHost() {
        OkHttpClient customClient = new OkHttpClient.Builder().build();

        // Verify that the builder doesn't throw an exception when custom client is set without host
        Pinecone client = new Pinecone.Builder("testApiKey")
                .withOkHttpClient(customClient)
                .build();

        assertNotNull(client, "Pinecone client should be created successfully with custom OkHttpClient but no host");
    }
}