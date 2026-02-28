package com.auth.template.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    USER_ALREADY_EXISTED(3000, "User existed"),
    UNCATEGORIZED(9999, "Uncategorized Exception"),
    INVALID_USERNAME(3001, "Username must be between 3 and 50 characters"),
    USERNAME_REQUIRED(3002, "Username must not be blank"),
    INVALID_EMAIL(3003, "Invalid email"),
    EMAIL_REQUIRED(3004, "Email must not be blank"),
    PASSWORD_REQUIRED(3005, "Password must not be blank"),
    INVALID_PASSWORD_LENGTH(3006, "Password must be at least 8 characters"),
    INVALID_PASSWORD_PATTERN(3007, "Password must contain at least one uppercase letter, one lowercase letter, and one number"),
    INVALID_FULL_NAME(3008, "Full name must be between 3 and 50 characters"),
    ACCOUNT_LOCKED(3009, "Account is locked due to multiple failed login attempts"),
    JWT_GENERATE_TOKEN(2000, "Failed to generate JWT token"),
    JWT_PARSE_TOKEN(2001, "Failed to parse JWT token"),
    JWT_INVALID_TOKEN(2002, "Invalid JWT token"),
    INVALID_VERIFICATION_TOKEN(2003, "Invalid verification token"),
    TOKEN_EXPIRED(2004, "Verification token expired"),
    VERIFICATION_TOKEN_USED(2005, "Token has already been used"),
    INVALID_RESET_PASSWORD_TOKEN(2006, "Invalid reset password token"),
    ;

    private final int code;
    private final String message;
}
