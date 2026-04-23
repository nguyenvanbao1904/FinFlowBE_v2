package com.finflow.backend.identity.application.usecase;

import com.finflow.backend.identity.api.AccountSoftDeletedEvent;
import com.finflow.backend.identity.application.command.DeleteAccountCommand;
import com.finflow.backend.identity.application.port.in.DeleteAccountPort;
import com.finflow.backend.identity.application.port.out.PasswordEncoderPort;
import com.finflow.backend.identity.domain.entity.User;
import com.finflow.backend.identity.domain.repository.UserRepository;
import com.finflow.backend.identity.exception.IdentityErrorCode;
import com.finflow.backend.common.exception.AppException;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class DeleteAccountUseCase implements DeleteAccountPort {
    private final UserRepository userRepository;
    private final PasswordEncoderPort passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    @Override
    public void execute(DeleteAccountCommand command) {
        User user = userRepository.findById(command.userId())
                .orElseThrow(() -> new AppException(IdentityErrorCode.USER_NOT_FOUND));

        // Logic:
        // 1. If user has password (local user) -> command.password is MANDATORY and MUST match.
        // 2. If user has NO password (google user) -> command.password is IGNORED.
        
        boolean hasPasswordInDb = user.getPassword() != null && !user.getPassword().isEmpty();

        if (hasPasswordInDb) {
            if (command.password() == null || command.password().isEmpty()) {
                throw new AppException(IdentityErrorCode.PASSWORD_REQUIRED);
            }
            if (!passwordEncoder.matches(command.password(), user.getPassword())) {
                throw new AppException(IdentityErrorCode.INVALID_PASSWORD);
            }
        }

        // Soft delete: Mark as inactive and set deletion timestamp
        user.setIsActive(false);
        user.setDeletedAt(LocalDateTime.now());
        userRepository.save(user);

        // Publish event for email notification
        String correlationId = MDC.get("correlationId");
        eventPublisher.publishEvent(
                AccountSoftDeletedEvent.builder()
                        .email(user.getEmail())
                        .username(user.getUsername())
                        .correlationId(correlationId)
                        .build()
        );
    }
}
