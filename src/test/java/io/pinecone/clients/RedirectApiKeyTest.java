package io.pinecone.clients;

import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openapitools.db_control.client.ApiClient;
import org.openapitools.db_control.client.Configuration;
import org.openapitools.db_control.client.api.ManageIndexesApi;

import static org.junit.jupiter.api.Assertions.*;

/**
 * OkHttp follows redirects and strips Authorization on a cross-origin hop, but not Api-Key.
 */
public class RedirectApiKeyTest {
    private static final String API_KEY = "sk-SENTINEL-do-not-log";

    private MockWebServer origin;
    private MockWebServer other;

    @BeforeEach
    void setUp() throws Exception {
        origin = new MockWebServer();
        origin.start();
        other = new MockWebServer();
        other.start();
    }

    @AfterEach
    void tearDown() throws Exception {
        origin.shutdown();
        other.shutdown();
    }

    private void listIndexes() throws Exception {
        ApiClient apiClient = new ApiClient(Pinecone.buildOkHttpClient(null));
        apiClient.setBasePath(origin.url("").toString().replaceAll("/$", ""));
        apiClient.setApiKey(API_KEY);
        new ManageIndexesApi(apiClient).listIndexes(Configuration.VERSION);
    }

    @Test
    void apiKeyIsNotForwardedOnCrossOriginRedirect() throws Exception {
        origin.enqueue(new MockResponse().setResponseCode(307).setHeader("Location", other.url("/indexes")));
        other.enqueue(new MockResponse().setResponseCode(200).setBody("{\"indexes\":[]}"));

        listIndexes();

        assertEquals(API_KEY, origin.takeRequest().getHeader("Api-Key"));
        RecordedRequest redirected = other.takeRequest();
        assertEquals("/indexes", redirected.getPath());
        assertNull(redirected.getHeader("Api-Key"));
    }

    @Test
    void apiKeyIsKeptOnSameOriginRedirect() throws Exception {
        origin.enqueue(new MockResponse().setResponseCode(307).setHeader("Location", origin.url("/moved")));
        origin.enqueue(new MockResponse().setResponseCode(200).setBody("{\"indexes\":[]}"));

        listIndexes();

        assertEquals(API_KEY, origin.takeRequest().getHeader("Api-Key"));
        RecordedRequest redirected = origin.takeRequest();
        assertEquals("/moved", redirected.getPath());
        assertEquals(API_KEY, redirected.getHeader("Api-Key"));
    }

    private static boolean keeps(String from, String to) {
        return Pinecone.keepsApiKey(HttpUrl.get(from), HttpUrl.get(to));
    }

    @Test
    void apiKeyIsKeptOnSameHostHttpToHttpsUpgrade() {
        assertTrue(keeps("http://h/", "https://h/"));
        assertTrue(keeps("http://h:80/", "https://h:443/"));
    }

    @Test
    void apiKeyIsKeptOnSameOrigin() {
        assertTrue(keeps("https://h/a", "https://h/b"));
        assertTrue(keeps("http://h:8080/a", "http://h:8080/b"));
    }

    @Test
    void apiKeyIsDroppedOnHttpsToHttpDowngrade() {
        assertFalse(keeps("https://h/", "http://h/"));
    }

    @Test
    void apiKeyIsDroppedOnUpgradeWithNonDefaultPorts() {
        assertFalse(keeps("http://h:8080/", "https://h:8443/"));
        assertFalse(keeps("http://h/", "https://h:8443/"));
        assertFalse(keeps("http://h:8080/", "https://h/"));
    }

    @Test
    void apiKeyIsDroppedOnDifferentHost() {
        assertFalse(keeps("http://h/", "https://other/"));
        assertFalse(keeps("https://h/", "https://other/"));
    }

    @Test
    void apiKeyIsDroppedOnDifferentPort() {
        assertFalse(keeps("https://h/", "https://h:8443/"));
    }
}
