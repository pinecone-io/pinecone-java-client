package io.pinecone.helpers;

import java.util.Random;

public class RandomStringBuilder {
    private static final int MAX_RESOURCE_NAME_LENGTH = 45;

    public static String build(String prefix, int len) {
        String alphabet = "abcdefghijklmnopqrstuvwxyz";
        StringBuilder name = new StringBuilder();
        Random rnd = new Random();
        while (name.length() < len) {
            int index = (int) (rnd.nextFloat() * alphabet.length());
            name.append(alphabet.charAt(index));
        }
        return (prefix.isEmpty()) ? name.toString() : prefix + "-" + name;
    }

    public static String build(int len) {
        return build("", len);
    }

    /**
     * Builds a name for an index or collection, prefixed so {@link IndexCleanupUtility} can find what this run created.
     */
    public static String buildResourceName(String prefix, int len) {
        String name = IndexCleanupUtility.testResourcePrefix() + build(prefix, len);
        if (name.length() > MAX_RESOURCE_NAME_LENGTH) {
            throw new IllegalArgumentException("Resource name exceeds " + MAX_RESOURCE_NAME_LENGTH + " characters: " + name);
        }
        return name;
    }
}
