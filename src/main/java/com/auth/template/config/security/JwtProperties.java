package com.auth.template.config.security;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

/**
 * JWT Configuration Properties
 * Externalized configuration for JWT token settings
 */
@Configuration
@ConfigurationProperties(prefix = "jwt")
@Validated
public class JwtProperties {

    /**
     * Secret key for signing JWT tokens
     * Should be a strong, randomly generated key (minimum 256 bits for HS256)
     * In production, use environment variables or secure vault
     */
    @NotBlank(message = "JWT secret key must not be blank")
    private String secret;

    /**
     * Access token expiration time in milliseconds
     * Default: 1 hour (3600000 ms)
     */
    @NotNull(message = "Access token expiration must not be null")
    private Long accessTokenExpiration = 3600000L; // 1 hour

    /**
     * Refresh token expiration time in milliseconds
     * Default: 24 hours (86400000 ms)
     */
    @NotNull(message = "Refresh token expiration must not be null")
    private Long refreshTokenExpiration = 86400000L; // 24 hours

    /**
     * Token issuer (iss claim)
     * Identifies the principal that issued the JWT
     */
    private String issuer = "spring-boot-template-app";

    /**
     * Token audience (aud claim)
     * Identifies the recipients that the JWT is intended for
     */
    private String audience = "spring-boot-template-users";

    // Getters and Setters

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public Long getAccessTokenExpiration() {
        return accessTokenExpiration;
    }

    public void setAccessTokenExpiration(Long accessTokenExpiration) {
        this.accessTokenExpiration = accessTokenExpiration;
    }

    public Long getRefreshTokenExpiration() {
        return refreshTokenExpiration;
    }

    public void setRefreshTokenExpiration(Long refreshTokenExpiration) {
        this.refreshTokenExpiration = refreshTokenExpiration;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public String getAudience() {
        return audience;
    }

    public void setAudience(String audience) {
        this.audience = audience;
    }
}
