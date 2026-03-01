package com.auth.template.mapper;

import com.auth.template.modules.user.dto.request.RoleRequest;
import com.auth.template.modules.user.dto.response.RoleResponse;
import com.auth.template.modules.user.entity.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(componentModel = "spring")
public interface RoleMapper {

    RoleResponse toRoleResponse(Role role);

    @Mapping(target = "permissions", ignore = true)
    Role toRole(RoleRequest roleRequest);
}
