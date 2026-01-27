package com.auth.template.common.exception;

/**
 * Exception thrown when attempting to register with a username that already
 * exists
 */
public class UserAlreadyExistsException extends RuntimeException {

    public UserAlreadyExistsException(String username) {
        super("User with username '" + username + "' already exists");
    }
}
