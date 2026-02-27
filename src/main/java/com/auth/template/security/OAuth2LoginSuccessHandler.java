package com.auth.template.security;

import com.auth.template.common.constant.AuthProvider;
import com.auth.template.common.exception.UserAlreadyExistsException;
import com.auth.template.modules.auth.entity.UserPrincipal;
import com.auth.template.modules.auth.service.JWTService;
import com.auth.template.modules.user.entity.User;
import com.auth.template.modules.user.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JWTService jwtService;
    private final UserRepository userRepository;
    private static final Logger logger = LoggerFactory.getLogger(OAuth2LoginSuccessHandler.class);

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String providerId = oAuth2User.getAttribute("sub");

        String generatedUsername = email.split("@")[0];

        User user = userRepository.findByEmail(email).orElseGet(() -> {
            logger.info("Create new user from Google OAuth2: {}", email);
            User newUser = User.builder()
                    .username(generatedUsername)
                    .email(email)
                    .fullName(name)
                    .provider(AuthProvider.GOOGLE)
                    .providerId(providerId)
                    .emailVerified(true)
                    .enabled(true)
                    .imageUrl(oAuth2User.getAttribute("picture"))
                    .build();
            return newUser;
        });

        user.setLastLoginAt(LocalDateTime.now());
        User savedUser = userRepository.save(user);

        logger.info("User registered successfully with Oauth2: {}", savedUser.getUsername());

        String roleName = (savedUser.getRole() != null) ? savedUser.getRole().toString() : "ROLE_USER";

        UserPrincipal userPrincipal = new UserPrincipal(savedUser);

        String accessToken = jwtService.generateAccessToken(userPrincipal);
        String refreshToken = jwtService.generateRefreshToken(userPrincipal.getUsername());

        String targetUrl =
                "http://localhost:3000/oauth2/redirect?accessToken=" + accessToken + "&refreshToken=" + refreshToken;
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
