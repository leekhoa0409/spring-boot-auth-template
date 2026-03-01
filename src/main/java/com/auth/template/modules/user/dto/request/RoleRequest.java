package com.auth.template.modules.user.dto.request;

import lombok.*;

import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RoleRequest {
    private String roleName;
    private String description;
    private Set<String> permissions;
}
