package com.auth.template.mapper;

import com.auth.template.modules.user.dto.request.RoleRequest;
import com.auth.template.modules.user.dto.response.RoleResponse;
import com.auth.template.modules.user.entity.Permission;
import com.auth.template.modules.user.entity.Role;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-03-01T11:16:55+0700",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21 (Eclipse Adoptium)"
)
@Component
public class RoleMapperImpl implements RoleMapper {

    @Override
    public RoleResponse toRoleResponse(Role role) {
        if ( role == null ) {
            return null;
        }

        RoleResponse.RoleResponseBuilder roleResponse = RoleResponse.builder();

        roleResponse.roleName( role.getRoleName() );
        roleResponse.description( role.getDescription() );
        Set<Permission> set = role.getPermissions();
        if ( set != null ) {
            roleResponse.permissions( new LinkedHashSet<Permission>( set ) );
        }

        return roleResponse.build();
    }

    @Override
    public Role toRole(RoleRequest roleRequest) {
        if ( roleRequest == null ) {
            return null;
        }

        Role.RoleBuilder role = Role.builder();

        role.roleName( roleRequest.getRoleName() );
        role.description( roleRequest.getDescription() );

        return role.build();
    }
}
