package cit.edu.mmr.exception.exceptions;

/**
 * Exception thrown when attempting to access a disabled user account
 */
public class DisabledAccountException extends RuntimeException {
    public DisabledAccountException(String message) {
        super(message);
    }

    public DisabledAccountException(String message, Throwable cause) {
        super(message, cause);
    }
}