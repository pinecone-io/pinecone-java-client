package io.pinecone;

import io.pinecone.configs.PineconeConfig;
import io.pinecone.exceptions.PineconeConfigurationException;
import org.junit.jupiter.api.Test;

import static io.pinecone.commons.Constants.pineconeClientVersion;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class PineconeConfigTest {

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
    public void testGetUserAgentWithSourceTag() {
        PineconeConfig config = new PineconeConfig("testApiKey");
        config.setSourceTag("testSourceTag");
        assertEquals(config.getUserAgent(), "lang=java; pineconeClientVersion=" + pineconeClientVersion + "; source_tag=testsourcetag");
    }

    @Test
    public void testSourceTagIsNormalized() {
        PineconeConfig config = new PineconeConfig("testApiKey");
        config.setSourceTag("test source tag !! @@ ## :");
        assertEquals(config.getSourceTag(), "test_source_tag_:");

        config.setSourceTag("TEST SOURCE : Tag     ----");
        assertEquals(config.getSourceTag(), "test_source_:_tag");

        config.setSourceTag("TEST      SOURCE TAG 2.4.5");
        assertEquals(config.getSourceTag(), "test_source_tag_245");
    }
}
