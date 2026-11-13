package io.pinecone;

import io.pinecone.configs.PineconeConfig;
import io.pinecone.exceptions.PineconeConfigurationException;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static io.pinecone.commons.Constants.pineconeClientVersion;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    private static String validateCapturingStderr(PineconeConfig config) {
        PrintStream originalErr = System.err;
        ByteArrayOutputStream captured = new ByteArrayOutputStream();
        System.setErr(new PrintStream(captured, true));
        try {
            config.validate();
        } finally {
            System.setErr(originalErr);
        }
        return captured.toString();
    }

    @Test
    public void testValidateTrimsWhitespaceAndControlCharactersFromApiKeyWithWarning() {
        String[] keys = {"pcsk_FAKE\n", "pcsk_FAKE\r\n", "\tpcsk_FAKE ", " \t pcsk_FAKE \t ", "\u0000pcsk_FAKE\u001f"};
        for (String key : keys) {
            PineconeConfig config = new PineconeConfig(key);
            String log = validateCapturingStderr(config);
            assertEquals("pcsk_FAKE", config.getApiKey());
            assertTrue(log.contains("WARN"), "expected a warning, got: " + log);
            assertTrue(log.contains("Removed leading/trailing whitespace or control characters"), log);
            assertFalse(log.contains("pcsk"), "warning echoes the API key: " + log);
            assertFalse(log.contains("FAKE"), "warning echoes the API key: " + log);
        }
    }

    @Test
    public void testValidateWarnsOnlyOnceWhenRunTwice() {
        PineconeConfig config = new PineconeConfig("pcsk_FAKE\n");
        validateCapturingStderr(config);
        String secondLog = validateCapturingStderr(config);
        assertEquals("pcsk_FAKE", config.getApiKey());
        assertFalse(secondLog.contains("Removed leading/trailing"), secondLog);
    }

    @Test
    public void testValidateLogsNoWarningForCleanApiKey() {
        PineconeConfig config = new PineconeConfig("pcsk_FAKE");
        String log = validateCapturingStderr(config);
        assertEquals("pcsk_FAKE", config.getApiKey());
        assertFalse(log.contains("Removed leading/trailing"), log);
    }

    @Test
    public void testValidateKeepsInteriorSpacesInApiKey() {
        PineconeConfig config = new PineconeConfig(" pcsk FAKE ");
        config.validate();
        assertEquals("pcsk FAKE", config.getApiKey());
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
