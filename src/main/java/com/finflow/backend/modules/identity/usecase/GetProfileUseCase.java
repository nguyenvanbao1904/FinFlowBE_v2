package com.finflow.backend.modules.identity.usecase;

import com.finflow.backend.common.exception.AppException;
import com.finflow.backend.modules.identity.domain.entity.User;
import com.finflow.backend.modules.identity.domain.repository.UserRepository;
import com.finflow.backend.modules.identity.dto.UserResponse;
import com.finflow.backend.modules.identity.exception.IdentityErrorCode;
import com.finflow.backend.modules.identity.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

/**
 * UseCase to retrieve User Profile.
 * Encapsulates logic for fetching user, handling not found error, and mapping to DTO.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class GetProfileUseCase {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserResponse execute(String username) {
        log.info("Executing GetProfileUseCase for user: {}", username);

        // 1. Load user from database
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.error("User {} not found in database", username);
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
