package com.auth.template.modules.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Login request")
public class LoginRequest {

    @NotBlank(message = "USERNAME_REQUIRED")
    @Schema(description = "Username", example = "johndoe")
    private String username;

    @NotBlank(message = "PASSWORD_REQUIRED")
    @Schema(description = "Password", example = "SecurePass123")
    private String password;

    @Schema(description = "Remember me", example = "false")
    private Boolean rememberMe = false;
}
