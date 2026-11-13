package io.pinecone.clients;

import io.pinecone.exceptions.PineconeValidationException;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class SurrogateCheckTest {

    @Test
    void acceptsWellFormedStrings() {
        for (String ok : Arrays.asList(null, "", "ns-1", "café", "😀", "a😀b😁")) {
            assertDoesNotThrow(() -> SurrogateCheck.requireWellFormed("create namespace", "Namespace", ok));
        }
    }

    @Test
    void rejectsLoneSurrogatesAtTheirIndex() {
        assertRejectedAt("\uD83D", 0);
        assertRejectedAt("x\uD83D", 1);
        assertRejectedAt("x\uDE00", 1);
        assertRejectedAt("\uD83Dx", 0);
        assertRejectedAt("\uDE00\uD83D", 0);
        assertRejectedAt("\uD83D😀", 0);
        assertRejectedAt("😀\uDE00", 2);
    }

    @Test
    void messageMatchesTheCommonsCheck() {
        PineconeValidationException e = assertThrows(PineconeValidationException.class,
                () -> SurrogateCheck.requireWellFormed("upsert records", "Record id", "b\uDE00"));
        assertEquals("Invalid upsert records request. Record id contains an unpaired UTF-16 surrogate (\\uDE00) at "
                + "index 1, so it cannot be sent. This usually means a string was cut in the middle of a character "
                + "such as an emoji.", e.getMessage());
    }

    private static void assertRejectedAt(String value, int index) {
        PineconeValidationException e = assertThrows(PineconeValidationException.class,
                () -> SurrogateCheck.requireWellFormed("create namespace", "Namespace", value));
        assertTrue(e.getMessage().contains(" at index " + index + ","), e.getMessage());
    }
}
