package service;

public class CodedException extends Exception {
    final private int statusCode;

    public CodedException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }


    public CodedException(int statusCode, String message, Exception cause) {
        super(message, cause);
        this.statusCode = statusCode;
    }


    @Override
    public String toString() {
        var cause = getCause();
        if (cause != null) {
            return String.format("%s: %s", getMessage(), cause.getMessage());

        }

        return super.toString();
    }

    public int statusCode() {
        return statusCode;
    }
}
