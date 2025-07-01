package dataaccess;

/**
 * Indicates there was an error connecting to the database
 */
public class DataAccessException extends Exception {
    final private int statusCode;

    public DataAccessException(String message) {
        this(0, message, null);
    }

    public DataAccessException(String message, Throwable ex) {
        this(0, message, ex);
    }

    public DataAccessException(int statusCode, String message, Throwable ex) {
        super(message, ex);
        this.statusCode = statusCode;
    }

    public int statusCode() {
        return statusCode;
    }
}
