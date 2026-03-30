package com.finflow.backend.identity.application.usecase;

import com.finflow.backend.identity.domain.entity.User;
import com.finflow.backend.identity.domain.repository.UserRepository;
import com.finflow.backend.identity.exception.IdentityErrorCode;
import com.finflow.backend.common.exception.AppException;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.finflow.backend.identity.presentation.request.DeleteAccountRequest;
import org.springframework.security.crypto.password.PasswordEncoder;

@Component
@RequiredArgsConstructor
public class DeleteAccountUseCase {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final org.springframework.context.ApplicationEventPublisher eventPublisher;

    @Transactional
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public void execute(String userId, DeleteAccountRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(IdentityErrorCode.USER_NOT_FOUND));

        // Logic:
        // 1. If user has password (local user) -> request.password is MANDATORY and MUST match.
        // 2. If user has NO password (google user) -> request.password is IGNORED (assumed frontend verified PIN).
        
        boolean hasPasswordInDb = user.getPassword() != null && !user.getPassword().isEmpty();

        if (hasPasswordInDb) {
            if (request.getPassword() == null || request.getPassword().isEmpty()) {
                throw new AppException(IdentityErrorCode.PASSWORD_REQUIRED);
            }
            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                throw new AppException(IdentityErrorCode.INVALID_PASSWORD);
            }
        }

        // Soft delete: Mark as inactive and set deletion timestamp
        user.setIsActive(false);
        user.setDeletedAt(java.time.LocalDateTime.now());
        userRepository.save(user);

        // Publish event for email notification
        String correlationId = MDC.get("correlationId");
        eventPublisher.publishEvent(
                com.finflow.backend.identity.application.event.AccountSoftDeletedEvent.builder()
                        .email(user.getEmail())
                        .username(user.getUsername())
                        .correlationId(correlationId)
                        .build()
        );
    }
}
