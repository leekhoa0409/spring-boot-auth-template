package com.auth.template.modules.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RoleRequest {

    @NotBlank(message = "Role name is required")
    @Schema(description = "Role name", example = "ADMIN")
    private String roleName;

    @NotBlank(message = "Description is required")
    @Schema(description = "Description", example = "Admin role")
    private String description;

    @NotBlank(message = "Permission is required")
    @Schema(description = "List of permission", example = "[READ, WRITE, DELETE]")
    private Set<String> permissions;
}
