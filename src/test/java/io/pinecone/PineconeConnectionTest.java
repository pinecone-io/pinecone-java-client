package io.pinecone;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PineconeConnectionTest {

    @Test
    void testGetEndpointWithConnectionUrl() {
        // Setting connection-url via PineconeConfigs()
        PineconeConfigs configs = new PineconeConfigs("api-key",
                "https://steps-784-123-eqasas0aaaa1213aasasc-1223-f1eea9.svc.production.pinecone.io");
        PineconeClientConfig clientConfig = configs.getClientConfig();
        PineconeConnectionConfig connectionConfig = configs.getConnectionConfig();

        String endpoint = PineconeConnection.getEndpoint(clientConfig, connectionConfig);

        assertEquals("https://steps-784-123-eqasas0aaaa1213aasasc-1223-f1eea9.svc.production.pinecone.io", endpoint);
    }

    @Test
    void testGetEndpointWithoutConnectionUrl() {
        // Connection-url is never set
        PineconeClientConfig clientConfig = new PineconeClientConfig()
                .withApiKey("secret-api-key")
                .withEnvironment("aws-us-east4")
                .withProjectName("fee911a");
        PineconeConnectionConfig connectionConfig = new PineconeConnectionConfig()
                .withIndexName("step-2");

        String endpoint = PineconeConnection.getEndpoint(clientConfig, connectionConfig);

        assertEquals("step-2-fee911a.svc.aws-us-east4.pinecone.io", endpoint);
    }
}
