package com.finflow.backend.identity.application.usecase;

import com.finflow.backend.identity.application.port.in.ToggleBiometricPort;

import com.finflow.backend.common.exception.AppException;
import com.finflow.backend.identity.domain.entity.User;
import com.finflow.backend.identity.domain.repository.UserRepository;
import com.finflow.backend.identity.application.command.ToggleBiometricCommand;
import com.finflow.backend.identity.exception.IdentityErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class ToggleBiometricUseCase implements ToggleBiometricPort {

    private final UserRepository userRepository;

    @Transactional
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @Override
    public void execute(ToggleBiometricCommand command) {
        User user = userRepository.findById(command.userId())
                .orElseThrow(() -> new AppException(IdentityErrorCode.USER_NOT_FOUND));

        user.setIsBiometricEnabled(command.enabled());
        userRepository.save(user);

        log.info("Biometric authentication {} for userId: {}", 
            command.enabled() ? "enabled" : "disabled", command.userId());
    }
}
