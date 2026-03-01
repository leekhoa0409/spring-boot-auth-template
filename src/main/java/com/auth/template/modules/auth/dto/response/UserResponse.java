package com.auth.template.modules.auth.dto.response;

import com.auth.template.common.constant.AuthProvider;
import com.auth.template.modules.user.entity.Role;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "User information response")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponse {

    @Schema(description = "User ID", example = "1")
    private UUID id;

    @Schema(description = "Username", example = "johndoe")
    private String username;

    @Schema(description = "Email", example = "john@example.com")
    private String email;

    @Schema(description = "Full name", example = "John Doe")
    private String fullName;

    @Schema(description = "Profile image URL")
    private String imageUrl;

    @Schema(description = "Email verified status", example = "true")
    private Boolean emailVerified;

    @Schema(description = "Account enabled status", example = "true")
    private Boolean enabled;

    @Schema(description = "Authentication provider", example = "LOCAL")
    private AuthProvider provider;

    @Schema(description = "User role", example = "ROLE_USER")
    private Set<Role> roles;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Account creation timestamp", example = "2026-01-24 10:00:00")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Last login timestamp", example = "2026-01-24 10:30:00")
    private LocalDateTime lastLoginAt;
}
