package io.pinecone;

import okhttp3.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;

import java.io.IOException;
import static org.mockito.Mockito.*;

import okhttp3.mockwebserver.MockWebServer;
import java.util.concurrent.ExecutorService;

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
    public void testDeleteIndex() throws IOException {
        String indexName = "testIndex";
        PineconeClientConfig clientConfig = new PineconeClientConfig().withApiKey("testApiKey");

        Call mockCall = mock(Call.class);
        when(mockCall.execute()).thenReturn(new Response.Builder()
                .request(new Request.Builder().url("http://localhost").build())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(ResponseBody.create(MediaType.parse("text/plain"), "Response body"))
                .build());

        OkHttpClient mockClient = mock(OkHttpClient.class);
        Dispatcher mockDispatcher = mock(Dispatcher.class);
        ExecutorService mockExecutorService = mock(ExecutorService.class);
        ConnectionPool mockConnectionPool = mock(ConnectionPool.class);
        when(mockClient.newCall(any(Request.class))).thenReturn(mockCall);
        when(mockClient.dispatcher()).thenReturn(mockDispatcher);
        when(mockClient.dispatcher().executorService()).thenReturn(mockExecutorService);
        when(mockClient.connectionPool()).thenReturn(mockConnectionPool);

        PineconeIndexOperationClient indexOperationClient = new PineconeIndexOperationClient(clientConfig, mockClient);
        indexOperationClient.deleteIndex(indexName);

        verify(mockClient, times(1)).newCall(any(Request.class));
        verify(mockCall, times(1)).execute();
        verify(mockDispatcher, times(1)).executorService();
        verify(mockExecutorService, times(1)).shutdown();
        verify(mockConnectionPool, times(1)).evictAll();
    }
}
