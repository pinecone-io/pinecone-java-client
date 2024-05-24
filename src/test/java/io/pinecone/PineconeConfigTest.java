package io.pinecone;

import io.pinecone.configs.PineconeConfig;
import io.pinecone.exceptions.PineconeConfigurationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PineconeConfigTest {

    private static final String pineconeClientVersion = "v1.1.1";

    @Test
    public void testValidateWithNullApiKey() {
        try {
            new PineconeConfig(null);
        } catch (PineconeConfigurationException expected) {
            assertEquals(expected.getLocalizedMessage(), "The API key is required and must not be empty or null");
        }
    }
    @Test
    public void testValidateWithEmptyApiKey() {
        try {
            new PineconeConfig("");
        } catch (PineconeConfigurationException expected) {
            assertEquals(expected.getLocalizedMessage(), "The API key is required and must not be empty or null");
        }
    }

    @Test
    public void testGetUserAgent() {
        PineconeConfig config = new PineconeConfig("testApiKey");
        assertEquals(config.getUserAgent(), "lang=java; pineconeClientVersion=" + pineconeClientVersion);
    }

    @Test
    public void testGetUserAgentGrpc() {
        PineconeConfig config = new PineconeConfig("testApiKey");
        assertEquals(config.getUserAgentGrpc(), "lang=java; pineconeClientVersion[grpc]=" + pineconeClientVersion);
    }
    @Test
    public void testGetUserAgentWithSourceTag() {
        PineconeConfig config = new PineconeConfig("testApiKey");
        config.setSourceTag("testSourceTag");
        assertEquals(config.getUserAgent(), "lang=java; pineconeClientVersion=" + pineconeClientVersion + "; source_tag=testsourcetag");
    }

    @Test
    public void testGetUserAgentGrpcWithSourceTag() {
        PineconeConfig config = new PineconeConfig("testApiKey");
        config.setSourceTag("testSourceTag");
        assertEquals(config.getUserAgentGrpc(), "lang=java; pineconeClientVersion[grpc]=" + pineconeClientVersion + "; source_tag=testsourcetag");
    }

    @Test
    public void testSourceTagIsNormalized() {
        PineconeConfig config = new PineconeConfig("testApiKey");
        config.setSourceTag("test source tag !! @@ ##");
        assertEquals(config.getSourceTag(), "test_source_tag");

        config.setSourceTag("TEST SOURCE Tag     ----");
        assertEquals(config.getSourceTag(), "test_source_tag");

        config.setSourceTag("TEST      SOURCE TAG 2.4.5");
        assertEquals(config.getSourceTag(), "test_source_tag_245");
    }
}
