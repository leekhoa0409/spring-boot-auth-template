package com.auth.template.modules.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Login request")
public class LoginRequest {

    @NotBlank(message = "Username is required")
    @Size(min = 5, max = 50, message = "Username must be between 3 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "Username can only contain letters, numbers, underscores and hyphens")
    @Schema(description = "Username", example = "johndoe")
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 6 characters")
    @Schema(description = "Password", example = "SecurePass123")
    private String password;

    @Schema(description = "Remember me", example = "false")
    private Boolean rememberMe = false;
}
