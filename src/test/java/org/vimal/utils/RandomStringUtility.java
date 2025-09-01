package org.vimal.utils;

import java.util.concurrent.ThreadLocalRandom;

public final class RandomStringUtility {
    private RandomStringUtility() {
    }

    private static final int DEFAULT_LENGTH = 10;
    private static final String ALPHA_NUMERIC_CHARACTER_SET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    public static String generateRandomStringAlphaNumeric() {
        return generateRandomString(ALPHA_NUMERIC_CHARACTER_SET, DEFAULT_LENGTH);
    }

    public static String generateRandomString(String characters, int length) {
        if (length < 1) {
            throw new IllegalArgumentException("Length must be positive to generate a random string");
        }
        if (characters == null) {
            throw new IllegalArgumentException("Character set must not be null");
        }
        if (characters.isEmpty()) {
            throw new IllegalArgumentException("Character set must not be empty");
        }
        StringBuilder sb = new StringBuilder(length);
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(characters.length());
            sb.append(characters.charAt(index));
        }
        return sb.toString();
    }
}
