package io.pinecone.commons;

import io.pinecone.exceptions.PineconeValidationException;

import java.util.List;

/**
 * Rejects ids and namespaces that hold an unpaired UTF-16 surrogate. Protobuf replaces such a character with
 * {@code ?} when it serializes the request, so without this check {@code "x\uD83D"} and {@code "x\uDE00"} would
 * both be sent as {@code "x?"}.
 */
final class SurrogateCheck {
    private SurrogateCheck() {
    }

    static void requireWellFormed(String request, String field, String value) {
        if (value == null) {
            return;
        }
        int length = value.length();
        for (int i = 0; i < length; i++) {
            char c = value.charAt(i);
            if (Character.isHighSurrogate(c) && i + 1 < length && Character.isLowSurrogate(value.charAt(i + 1))) {
                i++;
            } else if (Character.isSurrogate(c)) {
                throw new PineconeValidationException(String.format(
                        "Invalid %s request. %s contains an unpaired UTF-16 surrogate (\\u%04X) at index %d, so it "
                                + "cannot be sent. This usually means a string was cut in the middle of a character "
                                + "such as an emoji.",
                        request, field, (int) c, i));
            }
        }
    }

    static void requireWellFormed(String request, String field, List<String> values) {
        if (values == null) {
            return;
        }
        for (String value : values) {
            requireWellFormed(request, field, value);
        }
    }
}
