package com.finflow.backend.modules.identity.application.usecase;

import com.finflow.backend.modules.identity.domain.repository.UserRepository;
import com.finflow.backend.modules.identity.presentation.request.CheckUserExistenceRequest;
import com.finflow.backend.modules.identity.presentation.response.CheckUserExistenceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class CheckUserExistenceUseCase {

    private final UserRepository userRepository;

    public CheckUserExistenceResponse execute(CheckUserExistenceRequest request) {
        boolean emailExists = false;
        boolean usernameExists = false;
        Boolean isActive = null;
        Boolean hasPassword = null;
        Boolean isDeleted = null;

        if (StringUtils.hasText(request.getEmail())) {
            var userOpt = userRepository.findByEmail(request.getEmail());
            if (userOpt.isPresent()) {
                emailExists = true;
                var user = userOpt.get();
                isActive = user.getIsActive();
                hasPassword = user.getPassword() != null && !user.getPassword().isEmpty();
                isDeleted = user.getDeletedAt() != null;
            }
        }
        if (StringUtils.hasText(request.getUsername())) {
            usernameExists = userRepository.existsByUsername(request.getUsername());
        }

        boolean exists = emailExists || usernameExists;

        return CheckUserExistenceResponse.builder()
                .exists(exists)
                .isActive(isActive)
                .hasPassword(hasPassword)
                .isDeleted(isDeleted)
                .build();
    }
}
