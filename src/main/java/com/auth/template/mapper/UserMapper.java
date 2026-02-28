package com.auth.template.mapper;

import com.auth.template.modules.auth.dto.request.RegisterRequest;
import com.auth.template.modules.auth.dto.response.UserResponse;
import com.auth.template.modules.user.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponse toUserResponse(User user);
}
