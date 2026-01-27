package com.auth.template.common.exception;

/**
 * Exception thrown when account is locked
 */
public class AccountLockedException extends RuntimeException {

    public AccountLockedException(String message) {
        super(message);
    }
}
