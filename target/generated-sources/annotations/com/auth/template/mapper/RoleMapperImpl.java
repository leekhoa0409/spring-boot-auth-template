package com.auth.template.mapper;

import com.auth.template.modules.auth.dto.request.RoleRequest;
import com.auth.template.modules.auth.dto.response.RoleResponse;
import com.auth.template.modules.auth.entity.Permission;
import com.auth.template.modules.auth.entity.Role;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-04-26T21:28:59+0700",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.46.0.v20260407-0427, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class RoleMapperImpl implements RoleMapper {

    @Override
    public RoleResponse toRoleResponse(Role role) {
        if ( role == null ) {
            return null;
        }

        RoleResponse.RoleResponseBuilder roleResponse = RoleResponse.builder();

        roleResponse.description( role.getDescription() );
        Set<Permission> set = role.getPermissions();
        if ( set != null ) {
            roleResponse.permissions( new LinkedHashSet<Permission>( set ) );
        }
        roleResponse.roleName( role.getRoleName() );

        return roleResponse.build();
    }

    @Override
    public Role toRole(RoleRequest roleRequest) {
        if ( roleRequest == null ) {
            return null;
        }

        Role.RoleBuilder role = Role.builder();

        role.description( roleRequest.getDescription() );
        role.roleName( roleRequest.getRoleName() );

        return role.build();
    }
}
