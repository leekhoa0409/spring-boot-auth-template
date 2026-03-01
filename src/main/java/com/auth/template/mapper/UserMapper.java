package com.auth.template.mapper;

import com.auth.template.modules.auth.dto.response.UserResponse;
import com.auth.template.modules.user.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "roles", ignore = true)
    UserResponse toUserResponse(User user);
}
