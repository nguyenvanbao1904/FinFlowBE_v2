package com.finflow.backend.identity.application.usecase;

import com.finflow.backend.identity.application.port.in.GetProfilePort;
import com.finflow.backend.identity.application.query.GetProfileQuery;

import com.finflow.backend.common.exception.AppException;
import com.finflow.backend.identity.application.dto.UserOutput;
import com.finflow.backend.identity.domain.entity.User;
import com.finflow.backend.identity.domain.repository.UserRepository;
import com.finflow.backend.identity.exception.IdentityErrorCode;
import com.finflow.backend.identity.application.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class GetProfileUseCase implements GetProfilePort {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Transactional(readOnly = true)
    @Override
    public UserOutput execute(GetProfileQuery request) {
        String userId = request.userId();
        log.info("Executing GetProfileUseCase for userId: {}", userId);

        // 1. Load user from database
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("User ID {} not found in database", userId);
                    return new AppException(IdentityErrorCode.USER_NOT_FOUND);
                });

        // 2. Map entity to DTO
        UserOutput response = userMapper.toUserOutput(user);

        // 3. Map roles
        return UserOutput.builder()
                .id(response.id())
                .username(response.username())
                .email(response.email())
                .firstName(response.firstName())
                .lastName(response.lastName())
                .dob(response.dob())
                .isBiometricEnabled(response.isBiometricEnabled())
                .hasPassword(response.hasPassword())
                .roles(user.getRoles().stream().map(role -> role.getName()).collect(Collectors.toSet()))
                .build();
    }
}
