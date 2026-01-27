package com.auth.template.common.exception;

import org.springframework.security.core.AuthenticationException;

/**
 * Custom exception for JWT authentication errors
 * Extends AuthenticationException to integrate with Spring Security
 */
public class JwtAuthenticationException extends AuthenticationException {

    public JwtAuthenticationException(String message) {
        super(message);
    }

    public JwtAuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
