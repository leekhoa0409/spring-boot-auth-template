package com.auth.template.mapper;

import com.auth.template.modules.auth.dto.response.UserResponse;
import com.auth.template.modules.user.entity.User;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-04-26T21:28:59+0700",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.46.0.v20260407-0427, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public UserResponse toUserResponse(User user) {
        if ( user == null ) {
            return null;
        }

        UserResponse.UserResponseBuilder userResponse = UserResponse.builder();

        userResponse.createdAt( user.getCreatedAt() );
        userResponse.email( user.getEmail() );
        userResponse.emailVerified( user.getEmailVerified() );
        userResponse.enabled( user.getEnabled() );
        userResponse.fullName( user.getFullName() );
        userResponse.id( user.getId() );
        userResponse.imageUrl( user.getImageUrl() );
        userResponse.lastLoginAt( user.getLastLoginAt() );
        userResponse.provider( user.getProvider() );
        userResponse.username( user.getUsername() );

        return userResponse.build();
    }
}
