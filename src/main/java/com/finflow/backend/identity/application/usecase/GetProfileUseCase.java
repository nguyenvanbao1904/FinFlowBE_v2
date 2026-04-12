package com.finflow.backend.identity.application.usecase;

import com.finflow.backend.identity.application.port.in.GetProfilePort;

import com.finflow.backend.common.exception.AppException;
import com.finflow.backend.identity.domain.entity.User;
import com.finflow.backend.identity.domain.repository.UserRepository;
import com.finflow.backend.identity.presentation.response.UserResponse;
import com.finflow.backend.identity.exception.IdentityErrorCode;
import com.finflow.backend.identity.application.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class GetProfileUseCase implements GetProfilePort {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @Override
    public UserResponse execute(String userId) {
        log.info("Executing GetProfileUseCase for userId: {}", userId);

        // 1. Load user from database
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("User ID {} not found in database", userId);
                    return new AppException(IdentityErrorCode.USER_NOT_FOUND);
                });

        // 2. Map entity to DTO
        UserResponse response = userMapper.toUserResponse(user);

        // 3. Map roles
        response.setRoles(user.getRoles().stream()
                .map(role -> role.getName())
                .collect(Collectors.toSet()));

        return response;
    }
}
