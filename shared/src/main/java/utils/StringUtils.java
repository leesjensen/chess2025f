package utils;

public final class StringUtils {

    private StringUtils() {
        throw new AssertionError("Cannot instantiate utility class");
    }
    
    public static boolean isNullOrEmpty(String s) {
        return s == null || s.isEmpty();
    }
}
