package com.auth.template.mapper;

import com.auth.template.modules.auth.dto.request.PermissionRequest;
import com.auth.template.modules.auth.dto.response.PermissionResponse;
import com.auth.template.modules.auth.entity.Permission;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-03-02T23:18:13+0700",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21 (Eclipse Adoptium)"
)
@Component
public class PermissionMapperImpl implements PermissionMapper {

    @Override
    public PermissionResponse toPermissionResponse(Permission permission) {
        if ( permission == null ) {
            return null;
        }

        PermissionResponse.PermissionResponseBuilder permissionResponse = PermissionResponse.builder();

        permissionResponse.permission( permission.getPermission() );
        permissionResponse.description( permission.getDescription() );

        return permissionResponse.build();
    }

    @Override
    public Permission toPermission(PermissionRequest permissionRequest) {
        if ( permissionRequest == null ) {
            return null;
        }

        Permission.PermissionBuilder permission = Permission.builder();

        permission.permission( permissionRequest.getPermission() );
        permission.description( permissionRequest.getDescription() );

        return permission.build();
    }
}
