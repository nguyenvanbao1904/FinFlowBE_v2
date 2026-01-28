package com.finflow.backend.modules.identity.usecase;

import com.finflow.backend.common.exception.AppException;
import com.finflow.backend.modules.identity.domain.entity.User;
import com.finflow.backend.modules.identity.domain.repository.UserRepository;
import com.finflow.backend.modules.identity.dto.UpdateProfileRequest;
import com.finflow.backend.modules.identity.dto.UserResponse;
import com.finflow.backend.modules.identity.exception.IdentityErrorCode;
import com.finflow.backend.modules.identity.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UpdateProfileUseCase {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Transactional
    public UserResponse execute(String username, UpdateProfileRequest request) {
        User user = userRepository.findByUsername(username)
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
