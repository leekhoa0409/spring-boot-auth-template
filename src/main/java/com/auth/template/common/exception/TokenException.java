package com.auth.template.common.exception;

/**
 * Exception thrown when a token (verification or reset) is expired or invalid
 */
public class TokenException extends RuntimeException {

    public TokenException(String message) {
        super(message);
    }

    public TokenException(String message, Throwable cause) {
        super(message, cause);
    }
}
