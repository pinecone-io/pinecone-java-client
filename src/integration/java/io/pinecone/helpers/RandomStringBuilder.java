package io.pinecone.helpers;

import java.util.Random;

public class RandomStringBuilder {
    public static String build(String prefix, int len) {
        String alphabet = "abcdefghijklmnopqrstuvwxyz";
        StringBuilder name = new StringBuilder();
        Random rnd = new Random();
        while (name.length() < len) {
            int index = (int) (rnd.nextFloat() * alphabet.length());
            name.append(alphabet.charAt(index));
        }
        return prefix + "-" + name.toString();
    }
}
