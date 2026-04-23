package com.finflow.backend.identity.application.usecase;

import com.finflow.backend.identity.application.dto.CheckUserExistenceOutput;
import com.finflow.backend.identity.application.query.CheckUserExistenceQuery;
import com.finflow.backend.identity.application.port.in.CheckUserExistencePort;

import com.finflow.backend.identity.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class CheckUserExistenceUseCase implements CheckUserExistencePort {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    @Override
    public CheckUserExistenceOutput execute(CheckUserExistenceQuery query) {
        boolean emailExists = false;
        boolean usernameExists = false;
        Boolean isActive = null;
        Boolean hasPassword = null;
        Boolean isDeleted = null;

        if (StringUtils.hasText(query.email())) {
            var userOpt = userRepository.findByEmail(query.email());
            if (userOpt.isPresent()) {
                emailExists = true;
                var user = userOpt.get();
                isActive = user.getIsActive();
                hasPassword = user.getPassword() != null && !user.getPassword().isEmpty();
                isDeleted = user.getDeletedAt() != null;
            }
        }
        if (StringUtils.hasText(query.username())) {
            usernameExists = userRepository.existsByUsername(query.username());
        }

        boolean exists = emailExists || usernameExists;

        return CheckUserExistenceOutput.builder()
                .exists(exists)
                .isActive(isActive)
                .hasPassword(hasPassword)
                .isDeleted(isDeleted)
                .build();
    }
}
