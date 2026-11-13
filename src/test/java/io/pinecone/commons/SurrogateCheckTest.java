package io.pinecone.commons;

import io.pinecone.exceptions.PineconeValidationException;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class SurrogateCheckTest {

    @Test
    void acceptsWellFormedStrings() {
        for (String ok : Arrays.asList(null, "", "doc-1", "café", "😀", "a😀b😁")) {
            assertDoesNotThrow(() -> SurrogateCheck.requireWellFormed("upsert", "Vector id", ok));
        }
        assertDoesNotThrow(() -> SurrogateCheck.requireWellFormed("fetch", "Vector id", (java.util.List<String>) null));
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
    void checksEveryIdInAList() {
        PineconeValidationException e = assertThrows(PineconeValidationException.class,
                () -> SurrogateCheck.requireWellFormed("delete", "Vector id", Arrays.asList("a", "b\uDE00")));
        assertTrue(e.getMessage().startsWith("Invalid delete request. Vector id contains an unpaired UTF-16 "
                + "surrogate (\\uDE00) at index 1"), e.getMessage());
    }

    private static void assertRejectedAt(String value, int index) {
        PineconeValidationException e = assertThrows(PineconeValidationException.class,
                () -> SurrogateCheck.requireWellFormed("upsert", "Vector id", value));
        assertTrue(e.getMessage().contains(" at index " + index + ","), e.getMessage());
    }
}
