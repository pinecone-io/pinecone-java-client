package io.pinecone;

import io.pinecone.configs.PineconeClientConfig;
import io.pinecone.configs.PineconeConnection;
import io.pinecone.configs.PineconeConnectionConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PineconeConnectionTest {

    @Test
    void testGetEndpointWithConnectionUrlWithHttps() {
        PineconeClientConfig clientConfig = new PineconeClientConfig().withApiKey("api-key");
        PineconeConnectionConfig connectionConfig = new PineconeConnectionConfig()
                .withConnectionUrl("https://steps-784-123-eqasas0aaaa1213aasasc-1223-f1eea9.svc.production.pinecone.io");

        String endpoint = PineconeConnection.getEndpoint(connectionConfig.getConnectionUrl());

        assertEquals("steps-784-123-eqasas0aaaa1213aasasc-1223-f1eea9.svc.production.pinecone.io", endpoint);
    }

    @Test
    void testGetEndpointWithConnectionUrlWithHttp() {
        PineconeClientConfig clientConfig = new PineconeClientConfig().withApiKey("api-key");
        PineconeConnectionConfig connectionConfig = new PineconeConnectionConfig()
                .withConnectionUrl("http://steps-784-123-eqasas0aaaa1213aasasc-1223-f1eea9.svc.production.pinecone.io");

        String endpoint = PineconeConnection.getEndpoint(connectionConfig.getConnectionUrl());

        assertEquals("steps-784-123-eqasas0aaaa1213aasasc-1223-f1eea9.svc.production.pinecone.io", endpoint);
    }

    @Test
    void testGetEndpointWithoutConnectionUrl() {
        PineconeClientConfig clientConfig = new PineconeClientConfig()
                .withApiKey("secret-api-key")
                .withEnvironment("aws-us-east4")
                .withProjectName("fee911a");
        PineconeConnectionConfig connectionConfig = new PineconeConnectionConfig()
                .withIndexName("step-2");

        String endpoint = PineconeConnection.getEndpoint(connectionConfig.getConnectionUrl());

        assertEquals("step-2-fee911a.svc.aws-us-east4.pinecone.io", endpoint);
    }
}