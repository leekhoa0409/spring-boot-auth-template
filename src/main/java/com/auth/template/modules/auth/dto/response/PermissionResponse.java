package com.auth.template.modules.auth.dto.response;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PermissionResponse {
    private String permission;
    private String description;
}
