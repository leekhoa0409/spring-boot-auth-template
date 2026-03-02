package com.auth.template.modules.auth.service;

import com.auth.template.common.exception.AppException;
import com.auth.template.common.exception.ErrorCode;
import com.auth.template.modules.auth.entity.RefreshToken;
import com.auth.template.modules.auth.repository.RefreshTokenRepository;
import com.auth.template.modules.user.entity.User;
import com.auth.template.modules.user.repository.UserRepository;
import com.auth.template.security.JwtProperties;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class JWTService {

    private static final Logger logger = LoggerFactory.getLogger(JWTService.class);

    private final JwtProperties jwtProperties;
    private final RefreshTokenRepository refreshTokenRepository;

    @Autowired
    public JWTService(JwtProperties jwtProperties, RefreshTokenRepository refreshTokenRepository, UserRepository userRepository) {
        this.jwtProperties = jwtProperties;
        this.refreshTokenRepository = refreshTokenRepository;
        logger.info("JWTService initialized with issuer: {}", jwtProperties.getIssuer());
    }

    public String generateAccessToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();

        // Add roles/authorities to claims
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(role -> role.startsWith("ROLE_") ? role.substring(5) : role)
                .collect(Collectors.toList());
        claims.put("roles", roles);

        return generateToken(claims, userDetails.getUsername(), jwtProperties.getAccessTokenExpiration());
    }

//    public String generateRefreshToken(String username) {
//        Map<String, Object> claims = new HashMap<>();
//        claims.put("type", "refresh");
//
//        return generateToken(claims, username, jwtProperties.getRefreshTokenExpiration());
//    }

    public String generateRefreshToken(User user) {
        String token = UUID.randomUUID().toString();
        UUID userId = user.getId();
        refreshTokenRepository.save(RefreshToken.builder()
                .token(token)
                .userId(userId)
                .expiration(jwtProperties.getRefreshTokenExpiration())
                .build());
        return token;
    }

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
            throw new AppException(ErrorCode.JWT_GENERATE_TOKEN_FAILED);
        }
    }

    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            logger.error("Error parsing JWT token: {}", e.getMessage());
            throw new AppException(ErrorCode.JWT_PARSE_TOKEN_FAILED);
        }
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String extractUserName(String token) {
        try {
            return extractClaim(token, Claims::getSubject);
        } catch (Exception e) {
            logger.error("Error extracting username from token: {}", e.getMessage());
            throw new AppException(ErrorCode.JWT_INVALID_TOKEN);
        }
    }

    public List<String> extractRoles(String token) {
        try {
            Claims claims = extractAllClaims(token);

            List<?> rawRoles = claims.get("roles", List.class);

            if (rawRoles != null) {
                return rawRoles.stream()
                        .map(Object::toString)
                        .collect(Collectors.toList());
            }
            return new ArrayList<>();

        } catch (Exception e) {
            logger.warn("Error extracting roles from token: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtProperties.getSecret());
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public Long getAccessTokenExpirationInSeconds() {
        return jwtProperties.getAccessTokenExpiration() / 1000;
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public boolean validateAccessToken(String token, UserDetails userDetails) {
        try {
            final String username = extractUserName(token);
            final Claims claims = extractAllClaims(token);

            boolean usernameValid = username.equals(userDetails.getUsername());
            boolean notExpired = !isTokenExpired(token);
            boolean issuerValid = jwtProperties.getIssuer().equals(claims.getIssuer());
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

//    public boolean validateRefreshToken(String token) {
//        try {
//            final Claims claims = extractAllClaims(token);
//
//            String tokenType = claims.get("type", String.class);
//            if (!"refresh".equals(tokenType)) {
//                logger.warn("Token is not a refresh token");
//                return false;
//            }
//
//            boolean notExpired = !isTokenExpired(token);
//
//            boolean issuerValid = jwtProperties.getIssuer().equals(claims.getIssuer());
//
//            return notExpired && issuerValid;
//        } catch (Exception e) {
//            logger.error("Error validating refresh token: {}", e.getMessage());
//            return false;
//        }
//    }
}
