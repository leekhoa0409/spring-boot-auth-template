package com.auth.template.auth.service;

import com.auth.template.auth.dto.request.*;
import com.auth.template.auth.dto.response.AuthResponse;
import com.auth.template.auth.dto.response.UserResponse;
import com.auth.template.auth.entity.User;
import com.auth.template.auth.entity.UserPrincipal;
import com.auth.template.common.constant.AuthProvider;
import com.auth.template.common.exception.AccountLockedException;
import com.auth.template.common.exception.UserAlreadyExistsException;
import com.auth.template.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JWTService jwtService;
    private final TokenService tokenService;
    private final EmailService emailService;

    @Autowired
    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JWTService jwtService,
            TokenService tokenService,
            EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.tokenService = tokenService;
        this.emailService = emailService;
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        logger.info("Attempting to register user: {}", request.getUsername());

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UserAlreadyExistsException(request.getUsername());
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Email already in use");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .provider(AuthProvider.LOCAL)
                .emailVerified(false)
                .enabled(true)
                .build();

        User savedUser = userRepository.save(user);

        // Send verification email
        try {
            String token = tokenService.createVerificationToken(savedUser);
            emailService.sendVerificationEmail(savedUser.getEmail(), token);
        } catch (Exception e) {
            logger.error("Failed to send verification email", e);
        }

        logger.info("User registered successfully: {}", savedUser.getUsername());

        return mapToUserResponse(savedUser);
    }

    public AuthResponse login(LoginRequest request) {
        logger.info("Attempting to authenticate user: {}", request.getUsername());

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new org.springframework.security.core.userdetails.UsernameNotFoundException(
                        "User not found"));

        if (user.getAccountLocked()) {
            if (user.shouldUnlock()) {
                user.unlock();
                userRepository.save(user);
            } else {
                throw new AccountLockedException("Account is locked due to multiple failed login attempts");
            }
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()));

            if (authentication.isAuthenticated()) {
                user.resetFailedAttempts();
                user.setLastLoginAt(LocalDateTime.now());
                userRepository.save(user);

                UserDetails userDetails = (UserDetails) authentication.getPrincipal();

                String accessToken = jwtService.generateAccessToken(userDetails);
                String refreshToken = jwtService.generateRefreshToken(userDetails.getUsername());

                logger.info("User authenticated successfully: {}", request.getUsername());

                return AuthResponse.builder()
                        .accessToken(accessToken)
                        .refreshToken(refreshToken)
                        .expiresIn(jwtService.getAccessTokenExpirationInSeconds())
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .build();
            }
        } catch (Exception e) {
            user.incrementFailedAttempts();
            userRepository.save(user);
            throw e;
        }

        throw new RuntimeException("Authentication failed");
    }

    public AuthResponse refreshToken(RefreshTokenRequest request) {
        logger.info("Attempting to refresh token");

        if (!jwtService.validateRefreshToken(request.getRefreshToken())) {
            throw new RuntimeException("Invalid refresh token");
        }

        String username = jwtService.extractUserName(request.getRefreshToken());

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserDetails userDetails = new UserPrincipal(user);

        String newAccessToken = jwtService.generateAccessToken(userDetails);
        String newRefreshToken = jwtService.generateRefreshToken(username);

        logger.info("Token refreshed successfully for user: {}", username);

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .expiresIn(jwtService.getAccessTokenExpirationInSeconds())
                .username(user.getUsername())
                .email(user.getEmail())
                .build();
    }

    @Transactional
    public void verifyEmail(String token) {
        tokenService.verifyEmail(token);
    }

    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String token = tokenService.createPasswordResetToken(user);
        emailService.sendPasswordResetEmail(user.getEmail(), token);
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        User user = tokenService.validatePasswordResetToken(request.getToken());
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .imageUrl(user.getImageUrl())
                .emailVerified(user.getEmailVerified())
                .enabled(user.getEnabled())
                .provider(user.getProvider())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .lastLoginAt(user.getLastLoginAt())
                .build();
    }
}
