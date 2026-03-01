package com.auth.template.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    UNCATEGORIZED(9999, "Uncategorized Exception", HttpStatus.INTERNAL_SERVER_ERROR),


    USER_ALREADY_EXISTED(3000, "User existed", HttpStatus.BAD_REQUEST),
    INVALID_USERNAME(3001, "Username must be between 3 and 50 characters",  HttpStatus.BAD_REQUEST),
    USERNAME_REQUIRED(3002, "Username must not be blank", HttpStatus.BAD_REQUEST),
    INVALID_EMAIL(3003, "Invalid email", HttpStatus.BAD_REQUEST),
    EMAIL_REQUIRED(3004, "Email must not be blank", HttpStatus.BAD_REQUEST),
    PASSWORD_REQUIRED(3005, "Password must not be blank", HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD_LENGTH(3006, "Password must be at least 8 characters", HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD_PATTERN(3007, "Password must contain at least one uppercase letter, one lowercase letter, and " +
            "one number", HttpStatus.BAD_REQUEST),
    INVALID_FULL_NAME(3008, "Full name must be between 3 and 50 characters", HttpStatus.BAD_REQUEST),
    ACCOUNT_LOCKED(3009, "Account is locked due to multiple failed login attempts", HttpStatus.BAD_REQUEST),

    UNAUTHORIZED(2007, "Unauthorized", HttpStatus.UNAUTHORIZED),
    UNAUTHENTICATED(2008, "Unauthenticated", HttpStatus.FORBIDDEN),
    JWT_GENERATE_TOKEN(2000, "Failed to generate JWT token", HttpStatus.UNAUTHORIZED),
    JWT_PARSE_TOKEN(2001, "Failed to parse JWT token", HttpStatus.UNAUTHORIZED),
    JWT_INVALID_TOKEN(2002, "Invalid JWT token", HttpStatus.UNAUTHORIZED),
    INVALID_VERIFICATION_TOKEN(2003, "Invalid verification token", HttpStatus.UNAUTHORIZED),
    TOKEN_EXPIRED(2004, "Verification token expired", HttpStatus.UNAUTHORIZED),
    VERIFICATION_TOKEN_USED(2005, "Token has already been used", HttpStatus.UNAUTHORIZED),
    INVALID_RESET_PASSWORD_TOKEN(2006, "Invalid reset password token", HttpStatus.UNAUTHORIZED),
    ;

    private final int code;
    private final String message;
    private final HttpStatusCode httpStatusCode;
}
