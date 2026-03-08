package com.finflow.backend.identity.application.usecase;

import com.finflow.backend.common.exception.AppException;
import com.finflow.backend.identity.domain.entity.User;
import com.finflow.backend.identity.domain.repository.UserRepository;
import com.finflow.backend.identity.presentation.request.UpdateProfileRequest;
import com.finflow.backend.identity.presentation.response.UserResponse;
import com.finflow.backend.identity.exception.IdentityErrorCode;
import com.finflow.backend.identity.application.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class UpdateProfileUseCase {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Transactional
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public UserResponse execute(String userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(IdentityErrorCode.USER_NOT_FOUND));

        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        if (request.getDob() != null) {
            user.setDob(request.getDob());
        }

        User savedUser = userRepository.save(user);

        UserResponse response = userMapper.toUserResponse(savedUser);
        response.setRoles(savedUser.getRoles().stream()
                .map(role -> role.getName())
                .collect(Collectors.toSet()));

        return response;
    }
}
