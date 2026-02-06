package com.finflow.backend.modules.identity.application.mapper;

import com.finflow.backend.modules.identity.presentation.response.UserResponse;
import com.finflow.backend.modules.identity.domain.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "roles", ignore = true)
    UserResponse toUserResponse(User user);
}