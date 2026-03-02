package com.auth.template.mapper;

import com.auth.template.modules.auth.dto.request.RoleRequest;
import com.auth.template.modules.auth.dto.response.RoleResponse;
import com.auth.template.modules.auth.entity.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(componentModel = "spring")
public interface RoleMapper {

    RoleResponse toRoleResponse(Role role);

    @Mapping(target = "permissions", ignore = true)
    Role toRole(RoleRequest roleRequest);
}
