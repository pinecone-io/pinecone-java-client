package io.pinecone.clients;

import io.pinecone.exceptions.PineconeValidationException;

/**
 * Rejects namespaces, prefixes and record ids that hold an unpaired UTF-16 surrogate before {@link Index} and
 * {@link AsyncIndex} send them over REST, where such a character would be encoded as {@code ?}.
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
}
