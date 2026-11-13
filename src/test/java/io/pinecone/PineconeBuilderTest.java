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
    public void PineconeWithInvalidApiKeyCharactersIsRejectedWithoutEchoingKey() {
        String[] invalidKeys = {
                "pcsk_FA\nKE_SECRET",
                "pcsk_FA\r\nKE_SECRET",
                "pcsk_FA\u0000KE_SECRET",
                "pcsk_FA\u001fKE_SECRET",
                "pcsk_FA\tKE_SECRET",
                "pcsk_FAKE_SECRET\u007f",
                "pcsk_FAKE_SECRET_\u00e9",
                "\u00e9pcsk_FAKE_SECRET",
                "pcsk_FAK\u00e9_SECRET",
                "\r\n",
                "   ",
                "\t",
                " \t\u0000\n ",
        };
        for (String key : invalidKeys) {
            PineconeConfigurationException exception = assertThrows(PineconeConfigurationException.class,
                    () -> new Pinecone.Builder(key).build(), "expected rejection");
            for (Throwable t = exception; t != null; t = t.getCause()) {
                String message = String.valueOf(t.getMessage());
                assertFalse(message.contains("FA"), "message echoes the API key: " + message);
                assertFalse(message.contains("SECRET"), "message echoes the API key: " + message);
                assertFalse(message.contains("\u00e9"), "message echoes the API key: " + message);
            }
        }
    }

    @Test
    public void PineconeWithInvalidApiKeyCharacterReportsCodePointAndIndex() {
        PineconeConfigurationException exception = assertThrows(PineconeConfigurationException.class,
                () -> new Pinecone.Builder(" pcsk_FA\nKE_SECRET\n").build());
        assertTrue(exception.getMessage().contains("(U+000A) at index 7"), exception.getMessage());
    }

    @Test
    public void PineconeWithApiKeySurroundedByWhitespaceOrControlCharactersStillBuilds() {
        String[] keys = {"pcsk_FAKE\n", "pcsk_FAKE\r\n", "\tpcsk_FAKE ", " pcsk_FAKE", "pcsk_FAKE\u0000"};
        for (String key : keys) {
            assertNotNull(new Pinecone.Builder(key).build());
        }
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
}