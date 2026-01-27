package com.auth.template.common.constant;

/**
 * Application-wide constants
 */
public final class AppConstants {

    private AppConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    // Authentication
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";

    // Token Types
    public static final String TOKEN_TYPE_ACCESS = "access";
    public static final String TOKEN_TYPE_REFRESH = "refresh";

    // OAuth2
    public static final String OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME = "oauth2_auth_request";
    public static final String REDIRECT_URI_PARAM_COOKIE_NAME = "redirect_uri";
    public static final int OAUTH2_COOKIE_EXPIRE_SECONDS = 180;

    // Roles
    public static final String ROLE_USER = "ROLE_USER";
    public static final String ROLE_ADMIN = "ROLE_ADMIN";

    // Email
    public static final String EMAIL_VERIFICATION_SUBJECT = "Verify your email address";
    public static final String PASSWORD_RESET_SUBJECT = "Reset your password";
    public static final String WELCOME_SUBJECT = "Welcome to Auth Template!";

    // Account Lockout
    public static final int MAX_FAILED_ATTEMPTS = 5;
    public static final long LOCK_TIME_DURATION = 30 * 60 * 1000; // 30 minutes
}
