package io.pinecone;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class PineconeConfigTest {
    @Test
    public void testValidConnectionURL() {
        String apiKey = "secret-api-key";
        String connectionURL = "https://step-918-f1eea9.svc.production.pinecone.io";
        PineconeConfigs config = new PineconeConfigs(apiKey, connectionURL);

        assertNotNull(config.getClientConfig());
        assertNotNull(config.getConnectionConfig());
        assertEquals(apiKey, config.getClientConfig().getApiKey());
        assertEquals("f1eea9", config.getClientConfig().getProjectName());
        assertEquals("production", config.getClientConfig().getEnvironment());
        assertEquals("step-918", config.getConnectionConfig().getIndexName());
    }

    @Test
    public void testValidLengthIndexName() {
        // Index name can be 45 chars long
        String apiKey = "secret-api-key";
        String connectionURL = "https://steps-784-123-eqasas0aaaa1213aasasc-1223-f1eea9.svc.production.pinecone.io";
        PineconeConfigs config = new PineconeConfigs(apiKey, connectionURL);

        assertNotNull(config.getClientConfig());
        assertNotNull(config.getConnectionConfig());
        assertEquals(apiKey, config.getClientConfig().getApiKey());
        assertEquals("f1eea9", config.getClientConfig().getProjectName());
        assertEquals("production", config.getClientConfig().getEnvironment());
        assertEquals("steps-784-123-eqasas0aaaa1213aasasc-1223", config.getConnectionConfig().getIndexName());
    }

    @Test
    public void testSingleCharacterIndexName() {
        String apiKey = "api-key";
        String connectionURL = "https://a-abcdef123.svc.development.pinecone.io";

        PineconeConfigs config = new PineconeConfigs(apiKey, connectionURL);

        assertNotNull(config.getClientConfig());
        assertNotNull(config.getConnectionConfig());
        assertEquals(apiKey, config.getClientConfig().getApiKey());
        assertEquals("abcdef123", config.getClientConfig().getProjectName());
        assertEquals("development", config.getClientConfig().getEnvironment());
        assertEquals("a", config.getConnectionConfig().getIndexName());
    }

    @Test
    public void testInvalidLengthIndexName() {
        String apiKey = "api-key";
        String connectionURL = "https://steps-784-123-eqasas0aaaa1213aasasc-1223-f1eea91-projectName.svc.production.pinecone.io";

        assertThrows(PineconeValidationException.class, () -> new PineconeConfigs(apiKey, connectionURL));
    }

    @Test
    public void testUpperCaseIndexAndProjectName() {
        String apiKey = "secret-api-key";
        String connectionURL = "https://INDEX-UPPER-3901-A120EF.svc.development.pinecone.io";

        PineconeConfigs config = new PineconeConfigs(apiKey, connectionURL);

        assertNotNull(config.getClientConfig());
        assertNotNull(config.getConnectionConfig());
        assertEquals(apiKey, config.getClientConfig().getApiKey());
        assertEquals("A120EF", config.getClientConfig().getProjectName());
        assertEquals("development", config.getClientConfig().getEnvironment());
        assertEquals("INDEX-UPPER-3901", config.getConnectionConfig().getIndexName());
    }

    @Test
    public void testInvalidConnectionURL() {
        String apiKey = "secret-api-key";
        String connectionURL = "https://invalid-url";

        assertThrows(PineconeValidationException.class, () -> new PineconeConfigs(apiKey, connectionURL));
    }

    @Test
    public void testMissingIndexName() {
        String apiKey = "your-api-key";
        String connectionURL = "https://-projectName.svc.production.pinecone.io";

        assertThrows(PineconeValidationException.class, () -> new PineconeConfigs(apiKey, connectionURL));
    }

    @Test
    public void testMissingProjectName() {
        String apiKey = "secret-api-key";
        String connectionURL = "https://indexName.svc.production.pinecone.io";

        assertThrows(PineconeValidationException.class, () -> new PineconeConfigs(apiKey, connectionURL));
    }

    @Test
    public void testInvalidProjectName() {
        String apiKey = "your-api-key";
        // Project name must be hexadecimal
        String connectionURL = "https://indexName-url.svc.production.pinecone.io";

        assertThrows(PineconeValidationException.class, () -> new PineconeConfigs(apiKey, connectionURL));
    }


    @Test
    public void testInvalidSubDomain() {
        String apiKey = "your-api-key";
        String connectionURL = "https://my-index-abcdef123.wrongsubdomain.pinecone.io";

        assertThrows(PineconeValidationException.class, () -> new PineconeConfigs(apiKey, connectionURL));
    }

    @Test
    public void testMissingSubDomain() {
        String apiKey = "your-api-key";
        String connectionURL = "https://my-index-abcdef123.pinecone.io";

        assertThrows(PineconeValidationException.class, () -> new PineconeConfigs(apiKey, connectionURL));
    }

    @Test
    public void testInvalidDomain() {
        String apiKey = "your-api-key";
        String connectionURL = "https://my-index-abcdef123.svc.pinenotcone.io";

        assertThrows(PineconeValidationException.class, () -> new PineconeConfigs(apiKey, connectionURL));
    }

    @Test
    public void testMissingDomain() {
        String apiKey = "your-api-key";
        String connectionURL = "https://my-index-abcdef123.svc.io";

        assertThrows(PineconeValidationException.class, () -> new PineconeConfigs(apiKey, connectionURL));
    }
}