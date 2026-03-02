package com.auth.template.mapper;

import com.auth.template.modules.auth.dto.request.PermissionRequest;
import com.auth.template.modules.auth.dto.response.PermissionResponse;
import com.auth.template.modules.auth.entity.Permission;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PermissionMapper {
    PermissionResponse toPermissionResponse(Permission permission);
    Permission toPermission(PermissionRequest permissionRequest);
}
