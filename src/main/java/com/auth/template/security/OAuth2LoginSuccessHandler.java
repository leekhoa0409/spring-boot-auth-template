package com.auth.template.security;

import com.auth.template.common.constant.AuthProvider;
import com.auth.template.modules.auth.entity.CustomUserDetails;
import com.auth.template.modules.auth.entity.Role;
import com.auth.template.modules.auth.repository.RoleRepository;
import com.auth.template.modules.auth.service.JWTService;
import com.auth.template.modules.user.entity.User;
import com.auth.template.modules.user.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JWTService jwtService;
    private final UserRepository userRepository;
    private static final Logger logger = LoggerFactory.getLogger(OAuth2LoginSuccessHandler.class);
    private final RoleRepository roleRepository;

    @Value("${app.frontend-url}")
    private String frontEndUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String providerId = oAuth2User.getAttribute("sub");

        String generatedUsername = "";
        if (email != null && name != null) {
            generatedUsername = email.split("@")[0];
        }

        Optional<User> existingUser = userRepository.findByEmail(email);
        User user;

        if (existingUser.isPresent()) {
            user = existingUser.get();
        } else {
            logger.info("Create new user from Google OAuth2: {}", email);
            HashSet<Role> roles = new HashSet<>();
            roleRepository.findById("USER").ifPresent(role -> roles.add(role));
            user = User.builder()
                    .username(generatedUsername)
                    .email(email)
                    .fullName(name)
                    .provider(AuthProvider.GOOGLE)
                    .providerId(providerId)
                    .emailVerified(true)
                    .enabled(true)
                    .roles(roles)
                    .imageUrl(oAuth2User.getAttribute("picture"))
                    .build();
        }
        user.setLastLoginAt(LocalDateTime.now());
        User savedUser = userRepository.save(user);

        logger.info("User registered successfully with Oauth2: {}", savedUser.getUsername());

        CustomUserDetails customUserDetails = new CustomUserDetails(savedUser);

        String accessToken = jwtService.generateAccessToken(customUserDetails);
        String refreshToken = jwtService.generateRefreshToken(savedUser);

        String targetUrl =
                frontEndUrl + "/oauth2/redirect?accessToken=" + accessToken + "&refreshToken=" + refreshToken;
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
