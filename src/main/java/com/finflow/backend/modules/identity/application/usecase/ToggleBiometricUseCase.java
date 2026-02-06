package com.finflow.backend.modules.identity.application.usecase;

import com.finflow.backend.common.exception.AppException;
import com.finflow.backend.modules.identity.domain.entity.User;
import com.finflow.backend.modules.identity.domain.repository.UserRepository;
import com.finflow.backend.modules.identity.presentation.request.ToggleBiometricRequest;
import com.finflow.backend.modules.identity.exception.IdentityErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class ToggleBiometricUseCase {

    private final UserRepository userRepository;

    @Transactional
    public void execute(String username, ToggleBiometricRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(IdentityErrorCode.USER_NOT_FOUND));

        user.setIsBiometricEnabled(request.getEnabled());
        userRepository.save(user);

        log.info("Biometric authentication {} for user: {}", 
            request.getEnabled() ? "enabled" : "disabled", username);
    }
}
