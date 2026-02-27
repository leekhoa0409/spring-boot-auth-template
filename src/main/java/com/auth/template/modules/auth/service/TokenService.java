package com.auth.template.modules.auth.service;

import com.auth.template.modules.auth.entity.PasswordResetToken;
import com.auth.template.modules.auth.entity.VerificationToken;
import com.auth.template.common.exception.TokenException;
import com.auth.template.modules.user.entity.User;
import com.auth.template.modules.user.repository.PasswordResetTokenRepository;
import com.auth.template.modules.user.repository.UserRepository;
import com.auth.template.modules.user.repository.VerificationTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final VerificationTokenRepository verificationTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final UserRepository userRepository;

    @Value("${app.email.verification-expiration:86400000}")
    private Long verificationExpiration;

    @Value("${app.email.reset-expiration:3600000}")
    private Long resetExpiration;

    public String createVerificationToken(User user) {
        String token = UUID.randomUUID().toString();

        VerificationToken verificationToken = VerificationToken.builder()
                .token(token)
                .user(user)
                .expiryDate(LocalDateTime.now().plusSeconds(verificationExpiration / 1000))
                .build();

        verificationTokenRepository.save(verificationToken);
        return token;
    }

    @Transactional
    public void verifyEmail(String token) {
        VerificationToken verificationToken = verificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new TokenException("Invalid verification token"));

        if (verificationToken.getUsed()) {
            throw new TokenException("Token has already been used");
        }

        if (verificationToken.isExpired()) {
            throw new TokenException("Token has expired");
        }

        User user = verificationToken.getUser();
        user.setEmailVerified(true);
        userRepository.save(user);

        verificationToken.setUsed(true);
        verificationTokenRepository.save(verificationToken);
    }

    public String createPasswordResetToken(User user) {
        String token = UUID.randomUUID().toString();

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .user(user)
                .expiryDate(LocalDateTime.now().plusSeconds(resetExpiration / 1000))
                .build();

        passwordResetTokenRepository.save(resetToken);
        return token;
    }

    @Transactional
    public User validatePasswordResetToken(String token) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new TokenException("Invalid reset token"));

        if (resetToken.getUsed()) {
            throw new TokenException("Token has already been used");
        }

        if (resetToken.isExpired()) {
            throw new TokenException("Token has expired");
        }

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);

        return resetToken.getUser();
    }
}
