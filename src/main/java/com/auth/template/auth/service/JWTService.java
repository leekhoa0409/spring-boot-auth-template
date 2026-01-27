package com.auth.template.auth.service;

import com.auth.template.config.security.JwtProperties;
import com.auth.template.common.exception.JwtAuthenticationException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Service for JWT token generation and validation
 * Handles both access tokens and refresh tokens
 */
@Service
public class JWTService {

    private static final Logger logger = LoggerFactory.getLogger(JWTService.class);

    private final JwtProperties jwtProperties;

    @Autowired
    public JWTService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        logger.info("JWTService initialized with issuer: {}", jwtProperties.getIssuer());
    }

    /**
     * Generate access token for authenticated user
     * Contains user details and authorities
     */
    public String generateAccessToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();

        // Add roles/authorities to claims
        String roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));
        claims.put("roles", roles);

        return generateToken(claims, userDetails.getUsername(), jwtProperties.getAccessTokenExpiration());
    }

    /**
     * Generate refresh token for token renewal
     * Contains minimal information
     */
    public String generateRefreshToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "refresh");

        return generateToken(claims, username, jwtProperties.getRefreshTokenExpiration());
    }

    /**
     * Internal method to generate JWT token
     */
    private String generateToken(Map<String, Object> claims, String subject, Long expiration) {
        try {
            long currentTimeMillis = System.currentTimeMillis();

            return Jwts.builder()
                    .claims(claims)
                    .subject(subject)
                    .issuer(jwtProperties.getIssuer())
                    .audience().add(jwtProperties.getAudience()).and()
                    .issuedAt(new Date(currentTimeMillis))
                    .expiration(new Date(currentTimeMillis + expiration))
                    .signWith(getSigningKey())
                    .compact();
        } catch (Exception e) {
            logger.error("Error generating JWT token: {}", e.getMessage());
            throw new JwtAuthenticationException("Failed to generate JWT token", e);
        }
    }

    /**
     * Extract username from JWT token
     */
    public String extractUserName(String token) {
        try {
            return extractClaim(token, Claims::getSubject);
        } catch (Exception e) {
            logger.error("Error extracting username from token: {}", e.getMessage());
            throw new JwtAuthenticationException("Invalid JWT token", e);
        }
    }

    /**
     * Extract roles from JWT token
     */
    public String extractRoles(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return claims.get("roles", String.class);
        } catch (Exception e) {
            logger.warn("Error extracting roles from token: {}", e.getMessage());
            return "";
        }
    }

    /**
     * Validate access token
     */
    public boolean validateAccessToken(String token, UserDetails userDetails) {
        try {
            final String username = extractUserName(token);
            final Claims claims = extractAllClaims(token);

            // Validate username matches
            boolean usernameValid = username.equals(userDetails.getUsername());

            // Validate token not expired
            boolean notExpired = !isTokenExpired(token);

            // Validate issuer
            boolean issuerValid = jwtProperties.getIssuer().equals(claims.getIssuer());

            // Validate audience
            boolean audienceValid = claims.getAudience().contains(jwtProperties.getAudience());

            boolean isValid = usernameValid && notExpired && issuerValid && audienceValid;

            if (!isValid) {
                logger.warn("Token validation failed for user: {}", username);
            }

            return isValid;
        } catch (Exception e) {
            logger.error("Error validating access token: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Validate refresh token
     */
    public boolean validateRefreshToken(String token) {
        try {
            final Claims claims = extractAllClaims(token);

            // Check if it's a refresh token
            String tokenType = claims.get("type", String.class);
            if (!"refresh".equals(tokenType)) {
                logger.warn("Token is not a refresh token");
                return false;
            }

            // Validate token not expired
            boolean notExpired = !isTokenExpired(token);

            // Validate issuer
            boolean issuerValid = jwtProperties.getIssuer().equals(claims.getIssuer());

            return notExpired && issuerValid;
        } catch (Exception e) {
            logger.error("Error validating refresh token: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Extract specific claim from token
     */
    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extract all claims from token
     */
    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            logger.error("Error parsing JWT token: {}", e.getMessage());
            throw new JwtAuthenticationException("Failed to parse JWT token", e);
        }
    }

    /**
     * Check if token is expired
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Extract expiration date from token
     */
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Get signing key from secret
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtProperties.getSecret());
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Get access token expiration time in seconds
     */
    public Long getAccessTokenExpirationInSeconds() {
        return jwtProperties.getAccessTokenExpiration() / 1000;
    }
}
