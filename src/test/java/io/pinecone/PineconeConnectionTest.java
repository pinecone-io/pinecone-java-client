package io.pinecone;

import io.pinecone.configs.PineconeConnection;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PineconeConnectionTest {

    @Test
    void testGetEndpointWithConnectionUrlWithHttps() {
        String hostWithHttps = "https://steps-784-123-eqasas0aaaa1213aasasc-1223-f1eea9.svc.production.pinecone.io";
        String endpoint = PineconeConnection.formatEndpoint(hostWithHttps);

        assertEquals("steps-784-123-eqasas0aaaa1213aasasc-1223-f1eea9.svc.production.pinecone.io", endpoint);
    }

    @Test
    void testGetEndpointWithConnectionUrlWithHttp() {
        String hostWithHttp = "http://steps-784-123-eqasas0aaaa1213aasasc-1223-f1eea9.svc.production.pinecone.io";
        String endpoint = PineconeConnection.formatEndpoint(hostWithHttp);

        assertEquals("steps-784-123-eqasas0aaaa1213aasasc-1223-f1eea9.svc.production.pinecone.io", endpoint);
    }
}