package com.finflow.backend.modules.identity.application.usecase;

import com.finflow.backend.modules.identity.domain.entity.User;
import com.finflow.backend.modules.identity.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class CleanupDeletedAccountsUseCase {

    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Permanently delete accounts that have been soft-deleted longer than the configured retention period
     * and publish AccountHardDeletedEvent before removal.
     */
    @Transactional
    public void execute() {
        log.info("Starting scheduled account cleanup...");

        // 30 days retention policy
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30);

        List<User> usersToDelete = userRepository.findByDeletedAtBefore(cutoffDate);

        if (usersToDelete.isEmpty()) {
            log.info("No accounts found for permanent deletion.");
            return;
        }

        log.info("Found {} accounts to permanently delete.", usersToDelete.size());

        for (User user : usersToDelete) {
            try {
                // Publish event BEFORE deleting to ensure user still exists in memory (if listener needs it)
                String correlationId = MDC.get("correlationId");
                eventPublisher.publishEvent(
                        com.finflow.backend.modules.identity.application.event.AccountHardDeletedEvent.builder()
                                .email(user.getEmail())
                                .username(user.getUsername())
                                .correlationId(correlationId)
                                .build()
                );

                log.info("Permanently deleting user: {} (Deleted at: {})", user.getUsername(), user.getDeletedAt());
                userRepository.delete(user);
            } catch (Exception e) {
                log.error("Failed to delete user {}: {}", user.getUsername(), e.getMessage());
            }
        }

        log.info("Account cleanup completed.");
    }
}

