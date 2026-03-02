package com.auth.template.modules.auth.dto.response;

import com.auth.template.modules.auth.entity.Permission;
import lombok.*;

import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RoleResponse {
    private String roleName;
    private String description;
    private Set<Permission> permissions;
}
