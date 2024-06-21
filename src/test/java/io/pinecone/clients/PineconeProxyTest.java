package io.pinecone.clients;

import io.pinecone.configs.PineconeConfig;
import io.pinecone.configs.ProxyConfig;
import io.pinecone.exceptions.PineconeConfigurationException;
import okhttp3.OkHttpClient;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PineconeProxyTest {

    @Test
    void testConstructorAndGetters() {
        ProxyConfig proxyConfig = new ProxyConfig("localhost", 8080);
        assertEquals("localhost", proxyConfig.getHost());
        assertEquals(8080, proxyConfig.getPort());
    }

    @Test
    void testSetters() {
        ProxyConfig proxyConfig = new ProxyConfig("localhost", 8080);
        proxyConfig.setHost("127.0.0.1");
        proxyConfig.setPort(9090);

        assertEquals("127.0.0.1", proxyConfig.getHost());
        assertEquals(9090, proxyConfig.getPort());
    }

    @Test
    void testValidateInvalidHost() {
        ProxyConfig proxyConfig = new ProxyConfig(null, 8080);
        Exception exception = assertThrows(PineconeConfigurationException.class, proxyConfig::validate);
        assertEquals("Proxy host cannot be null or empty.", exception.getMessage());
    }

    @Test
    void testValidateEmptyHost() {
        ProxyConfig proxyConfig = new ProxyConfig("", 8080);
        Exception exception = assertThrows(PineconeConfigurationException.class, proxyConfig::validate);
        assertEquals("Proxy host cannot be null or empty.", exception.getMessage());
    }

    @Test
    void testValidateInvalidPort() {
        ProxyConfig proxyConfig = new ProxyConfig("localhost", 0);
        Exception exception = assertThrows(PineconeConfigurationException.class, proxyConfig::validate);
        assertEquals("Proxy port must be greater than 0.", exception.getMessage());
    }

    @Test
    void testValidateNegativePort() {
        ProxyConfig proxyConfig = new ProxyConfig("localhost", -1);
        Exception exception = assertThrows(PineconeConfigurationException.class, proxyConfig::validate);
        assertEquals("Proxy port must be greater than 0.", exception.getMessage());
    }

    @Test
    public void testBuildWithProxy() {
        String apiKey = "test-api-key";
        String proxyHost = "proxy.example.com";
        int proxyPort = 8080;

        Pinecone pinecone = new Pinecone.Builder(apiKey)
                .withProxy(proxyHost, proxyPort)
                .build();

        PineconeConfig config = pinecone.getConfig();
        assertEquals(proxyHost, config.getProxyConfig().getHost());
        assertEquals(proxyPort, config.getProxyConfig().getPort());
    }

    @Test
    public void testBothCustomOkHttpClientAndProxySet() {
        String apiKey = "test-api-key";
        OkHttpClient customOkHttpClient = new OkHttpClient();
        String proxyHost = "proxy.example.com";
        int proxyPort = 8080;

        Pinecone.Builder builder = new Pinecone.Builder(apiKey)
                .withOkHttpClient(customOkHttpClient)
                .withProxy(proxyHost, proxyPort);

        Exception exception = assertThrows(PineconeConfigurationException.class, builder::build);
        assertEquals("Invalid configuration: Both Custom OkHttpClient and Proxy are set. Please configure only one of these options.", exception.getMessage());
    }
}
