package com.finflow.backend.modules.identity.mapper;

import com.finflow.backend.modules.identity.dto.UserResponse;
import com.finflow.backend.modules.identity.domain.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring") // Để Spring quản lý như 1 Bean
public interface UserMapper {

    // MapStruct sẽ tự khớp các field trùng tên
    @Mapping(target = "roles", ignore = true) // Roles mình sẽ map tay cho an toàn hoặc viết custom mapper sau
    UserResponse toUserResponse(User user);
}