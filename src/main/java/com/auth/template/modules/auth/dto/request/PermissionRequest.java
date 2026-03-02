package com.auth.template.modules.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PermissionRequest {

    @NotBlank(message = "Permission is required")
    @Schema(description = "Permission", example = "READ")
    private String permission;

    @NotBlank(message = "Description is required")
    @Schema(description = "Description", example = "Read permission")
    private String description;
}
