package com.auth.template.mapper;

import com.auth.template.modules.auth.dto.request.PermissionRequest;
import com.auth.template.modules.auth.dto.response.PermissionResponse;
import com.auth.template.modules.auth.entity.Permission;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-04-26T21:28:59+0700",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.46.0.v20260407-0427, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class PermissionMapperImpl implements PermissionMapper {

    @Override
    public PermissionResponse toPermissionResponse(Permission permission) {
        if ( permission == null ) {
            return null;
        }

        PermissionResponse.PermissionResponseBuilder permissionResponse = PermissionResponse.builder();

        permissionResponse.description( permission.getDescription() );
        permissionResponse.permission( permission.getPermission() );

        return permissionResponse.build();
    }

    @Override
    public Permission toPermission(PermissionRequest permissionRequest) {
        if ( permissionRequest == null ) {
            return null;
        }

        Permission.PermissionBuilder permission = Permission.builder();

        permission.description( permissionRequest.getDescription() );
        permission.permission( permissionRequest.getPermission() );

        return permission.build();
    }
}
