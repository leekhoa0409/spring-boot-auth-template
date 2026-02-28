package com.auth.template.mapper;

import com.auth.template.modules.auth.dto.response.UserResponse;
import com.auth.template.modules.user.entity.User;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-02-28T15:37:04+0700",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21 (Eclipse Adoptium)"
)
@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public UserResponse toUserResponse(User user) {
        if ( user == null ) {
            return null;
        }

        UserResponse.UserResponseBuilder userResponse = UserResponse.builder();

        userResponse.id( user.getId() );
        userResponse.username( user.getUsername() );
        userResponse.email( user.getEmail() );
        userResponse.fullName( user.getFullName() );
        userResponse.imageUrl( user.getImageUrl() );
        userResponse.emailVerified( user.getEmailVerified() );
        userResponse.enabled( user.getEnabled() );
        userResponse.provider( user.getProvider() );
        userResponse.role( user.getRole() );
        userResponse.createdAt( user.getCreatedAt() );
        userResponse.lastLoginAt( user.getLastLoginAt() );

        return userResponse.build();
    }
}
