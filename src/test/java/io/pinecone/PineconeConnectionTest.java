package io.pinecone;

import io.pinecone.configs.PineconeConnection;
import io.pinecone.configs.PineconeConnectionConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PineconeConnectionTest {

    @Test
    void testGetEndpointWithConnectionUrlWithHttps() {
        PineconeConnectionConfig connectionConfig = new PineconeConnectionConfig()
                .withConnectionUrl("https://steps-784-123-eqasas0aaaa1213aasasc-1223-f1eea9.svc.production.pinecone.io");

        String endpoint = PineconeConnection.formatEndpoint(connectionConfig.getConnectionUrl());

        assertEquals("steps-784-123-eqasas0aaaa1213aasasc-1223-f1eea9.svc.production.pinecone.io", endpoint);
    }

    @Test
    void testGetEndpointWithConnectionUrlWithHttp() {
        PineconeConnectionConfig connectionConfig = new PineconeConnectionConfig()
                .withConnectionUrl("http://steps-784-123-eqasas0aaaa1213aasasc-1223-f1eea9.svc.production.pinecone.io");

        String endpoint = PineconeConnection.formatEndpoint(connectionConfig.getConnectionUrl());

        assertEquals("steps-784-123-eqasas0aaaa1213aasasc-1223-f1eea9.svc.production.pinecone.io", endpoint);
    }
}