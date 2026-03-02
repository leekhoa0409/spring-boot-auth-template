package com.auth.template.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    // 1xxx: SYSTEM & UNCATEGORIZED ERRORS
    UNCATEGORIZED(1999, "Uncategorized Exception", HttpStatus.INTERNAL_SERVER_ERROR),

    // 2xxx: AUTHENTICATION & AUTHORIZATION ERRORS
    UNAUTHENTICATED(2000, "Unauthenticated - Please login", HttpStatus.UNAUTHORIZED),
    ACCESS_DENIED(2001, "Access Denied - You do not have permission", HttpStatus.FORBIDDEN),

    JWT_GENERATE_TOKEN_FAILED(2002, "Failed to generate JWT token", HttpStatus.INTERNAL_SERVER_ERROR),
    JWT_PARSE_TOKEN_FAILED(2003, "Failed to parse JWT token", HttpStatus.UNAUTHORIZED),
    JWT_INVALID_TOKEN(2004, "Invalid JWT token", HttpStatus.UNAUTHORIZED),
    JWT_TOKEN_EXPIRED(2005, "JWT token has expired", HttpStatus.UNAUTHORIZED),

    INVALID_VERIFICATION_TOKEN(2006, "Invalid verification token", HttpStatus.UNAUTHORIZED),
    VERIFICATION_TOKEN_EXPIRED(2007, "Verification token expired", HttpStatus.UNAUTHORIZED),
    VERIFICATION_TOKEN_USED(2008, "Token has already been used", HttpStatus.BAD_REQUEST),
    INVALID_RESET_PASSWORD_TOKEN(2009, "Invalid reset password token", HttpStatus.UNAUTHORIZED),
    TOKEN_NOT_FOUND(20010, "Token not found", HttpStatus.UNAUTHORIZED),

    // 3xxx: USER MODULE ERRORS
    USER_NOT_FOUND(3000, "User not found", HttpStatus.NOT_FOUND),
    USER_ALREADY_EXISTED(3001, "User already existed", HttpStatus.CONFLICT), // Thực tế nên dùng 409 Conflict thay vì 400
    ACCOUNT_LOCKED(3002, "Account is locked due to multiple failed login attempts", HttpStatus.FORBIDDEN),

    // 4xxx: Data Validation Errors
    VALIDATION_ERROR(4000, "Validation Failed", HttpStatus.BAD_REQUEST)
    ;

    private final int code;
    private final String message;
    private final HttpStatusCode httpStatusCode;
}
