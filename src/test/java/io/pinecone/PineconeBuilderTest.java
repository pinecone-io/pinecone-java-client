package io.pinecone;

import io.pinecone.exceptions.PineconeConfigurationException;
import io.pinecone.clients.Pinecone;
import org.mockito.ArgumentCaptor;
import org.openapitools.db_control.client.model.*;
import okhttp3.*;
import org.junit.jupiter.api.Test;
import com.google.gson.Gson;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.AbstractMap;

import static io.pinecone.commons.Constants.pineconeClientVersion;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PineconeBuilderTest {
    private static final Gson gson = new Gson();

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
        try {
            new Pinecone.Builder(null).build();
        }
        catch(PineconeConfigurationException expected) {
            assertTrue(expected.getLocalizedMessage().contains("The API key is required and must not be empty or null"));
        }
    }

    @Test
    public void PineconeWithEmptyApiKey() {
        try {
            new Pinecone.Builder("").build();
        }
        catch(PineconeConfigurationException expected) {
            assertTrue(expected.getLocalizedMessage().contains("The API key is required and must not be empty or null"));
        }
    }

    @Test
    public void PineconeWithOkHttpClientAndUserAgent() throws IOException {
        String filePath = "src/test/resources/serverlessIndexJsonString.json";
        String indexJsonStringServerless = new String(Files.readAllBytes(Paths.get(filePath)));
        IndexModel expectedIndex = gson.fromJson(indexJsonStringServerless, IndexModel.class);

        AbstractMap.SimpleEntry<Call, OkHttpClient> mockCallAndClient =
                buildMockCallAndClient(ResponseBody.create(
                        indexJsonStringServerless,
                        MediaType.parse("application/json")));

        OkHttpClient mockClient = mockCallAndClient.getValue();

        Pinecone client = new Pinecone.Builder("testAPiKey")
                .withOkHttpClient(mockClient)
                .build();
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
        IndexModel expectedIndex = gson.fromJson(indexJsonStringServerless, IndexModel.class);

        AbstractMap.SimpleEntry<Call, OkHttpClient> mockCallAndClient =
                buildMockCallAndClient(ResponseBody.create(
                        indexJsonStringServerless,
                        MediaType.parse("application/json")));

        OkHttpClient mockClient = mockCallAndClient.getValue();

        Pinecone client = new Pinecone.Builder("testAPiKey")
                .withSourceTag("testSourceTag")
                .withOkHttpClient(mockClient)
                .build();
        IndexModel index = client.describeIndex("testIndex");

        ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);

        assertEquals(expectedIndex, index);
        verify(mockClient, times(1)).newCall(requestCaptor.capture());
        assertEquals("lang=java; pineconeClientVersion=" + pineconeClientVersion + "; source_tag=testSourceTag", requestCaptor.getValue().header("User-Agent"));
    }
}