package com.auth.template.modules.user.service;

import com.auth.template.mapper.UserMapper;
import com.auth.template.modules.auth.dto.request.ChangePasswordRequest;
import com.auth.template.modules.auth.dto.response.UserResponse;
import com.auth.template.modules.user.entity.User;
import com.auth.template.modules.user.repository.RoleRepository;
import com.auth.template.modules.user.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    public UserResponse getCurrentUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return userMapper.toUserResponse(user);
    }

    @Transactional
    public UserResponse updateProfile(String username, UserResponse updateRequest) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (updateRequest.getFullName() != null) {
            user.setFullName(updateRequest.getFullName());
        }

        User updatedUser = userRepository.save(user);
        logger.info("Profile updated for user: {}", username);

        return userMapper.toUserResponse(updatedUser);
    }

    @Transactional
    @CacheEvict(value = "users", key = "#username")
    public void changePassword(String username, ChangePasswordRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        logger.info("Password changed for user: {}", username);
    }

    @Transactional
    public void deleteAccount(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        userRepository.delete(user);
        logger.info("Account deleted for user: {}", username);
    }
}
