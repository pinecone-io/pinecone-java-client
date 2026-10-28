package io.pinecone.clients;

import okhttp3.OkHttpClient;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openapitools.db_control.client.ApiClient;
import org.openapitools.db_control.client.api.ManageIndexesApi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * PINECONE_DEBUG logs every control-plane request at OkHttp's BODY level; the API key must not appear in that output.
 */
public class DebugLoggingRedactionTest {
    private static final String API_KEY = "sk-SENTINEL-do-not-log";

    private final Logger okHttpLogger = Logger.getLogger(OkHttpClient.class.getName());
    private final List<String> logged = Collections.synchronizedList(new ArrayList<>());
    private final Handler capture = new Handler() {
        @Override
        public void publish(LogRecord record) {
            logged.add(record.getMessage());
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() {
        }
    };
    private MockWebServer server;

    @BeforeEach
    void setUp() throws Exception {
        okHttpLogger.addHandler(capture);
        server = new MockWebServer();
        server.start();
    }

    @AfterEach
    void tearDown() throws Exception {
        okHttpLogger.removeHandler(capture);
        server.shutdown();
    }

    @Test
    void debugLoggingRedactsApiKeyButStillSendsIt() throws Exception {
        server.enqueue(new MockResponse().setResponseCode(200).setBody("{\"indexes\":[]}"));
        ApiClient apiClient = new ApiClient(Pinecone.buildOkHttpClient(null));
        apiClient.setBasePath(server.url("").toString().replaceAll("/$", ""));
        apiClient.setApiKey(API_KEY);

        Pinecone.enableDebugLogging(apiClient);
        new ManageIndexesApi(apiClient).listIndexes(org.openapitools.db_control.client.Configuration.VERSION);

        RecordedRequest request = server.takeRequest();
        assertEquals(API_KEY, request.getHeader("Api-Key"));
        assertTrue(apiClient.isDebugging());

        String output = String.join("\n", logged);
        assertTrue(output.contains("--> GET"), output);
        assertTrue(output.contains("Api-Key: ██"), output);
        assertFalse(output.contains(API_KEY), output);
    }
}
