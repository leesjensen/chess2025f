package utils;

import java.util.*;

public final class StringUtils {

    private StringUtils() {
        throw new AssertionError("Cannot instantiate utility class");
    }

    public static boolean isNullOrEmpty(String s) {
        return s == null || s.isEmpty();
    }


    private static final Random RANDOM = new Random();

    public static String randomString() {
        long randomNumber = Math.abs(RANDOM.nextLong());
        return Long.toString(randomNumber, 36);
    }


    public static OptionalInt tryParseInt(String value) {
        try {
            return OptionalInt.of(Integer.parseInt(value));
        } catch (NumberFormatException e) {
            return OptionalInt.empty();
        }
    }
}
