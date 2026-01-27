package com.auth.template;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import io.github.cdimascio.dotenv.Dotenv;

/**
 * Main Application Class
 * Production-ready Spring Boot Authentication Template
 * 
 * Features:
 * - JWT Authentication (Access + Refresh Tokens)
 * - OAuth2 Login (Google, GitHub, Facebook)
 * - Email Verification
 * - Password Reset
 * - Swagger API Documentation
 * - Multi-environment Configuration
 * 
 * @author Auth Template
 * @version 1.0.0
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableCaching
public class AuthTemplateApplication {

    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
        dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));

        SpringApplication.run(AuthTemplateApplication.class, args);
    }
}
