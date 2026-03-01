package com.auth.template.security;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Setter
@Getter
@Configuration
@ConfigurationProperties(prefix = "jwt")
@Validated
public class JwtProperties {

    @NotBlank(message = "JWT secret key must not be blank")
    private String secret;

    @NotNull(message = "Access token expiration must not be null")
    private Long accessTokenExpiration = 3600000L; // 1 hour

    @NotNull(message = "Refresh token expiration must not be null")
    private Long refreshTokenExpiration = 86400000L; // 24 hours

    private String issuer = "auth-template-app";

    private String audience = "auth-template-users";
}
