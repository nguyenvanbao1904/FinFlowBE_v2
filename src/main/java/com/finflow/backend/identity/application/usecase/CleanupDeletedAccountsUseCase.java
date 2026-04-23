package com.finflow.backend.identity.application.usecase;

import com.finflow.backend.identity.api.AccountHardDeletedEvent;
import com.finflow.backend.identity.domain.entity.User;
import com.finflow.backend.identity.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.finflow.backend.identity.application.port.in.CleanupDeletedAccountsPort;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class CleanupDeletedAccountsUseCase implements CleanupDeletedAccountsPort {

    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Permanently delete accounts that have been soft-deleted longer than the configured retention period
     * and publish AccountHardDeletedEvent AFTER successful removal.
     */
    @Transactional
    @Override
    public void execute() {
        log.info("Starting scheduled account cleanup...");

        // 30 days retention policy
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30);
        int totalDeleted = 0;

        // Batch processing to avoid loading all deleted users into memory at once
        Page<User> batch;
        do {
            batch = userRepository.findByDeletedAtBefore(cutoffDate, PageRequest.of(0, 500));

            if (batch.isEmpty()) break;

            List<User> usersToDelete = batch.getContent();
            String correlationId = MDC.get("correlationId");
            List<AccountHardDeletedEvent> pendingEvents = new ArrayList<>(usersToDelete.size());
            for (User user : usersToDelete) {
                pendingEvents.add(AccountHardDeletedEvent.builder()
                        .email(user.getEmail())
                        .username(user.getUsername())
                        .correlationId(correlationId)
                        .build());
            }

            userRepository.deleteAll(usersToDelete);
            totalDeleted += usersToDelete.size();

            for (AccountHardDeletedEvent event : pendingEvents) {
                eventPublisher.publishEvent(event);
            }
        } while (batch.hasNext());

        if (totalDeleted == 0) {
            log.info("No accounts found for permanent deletion.");
        } else {
            log.info("Permanently deleted {} account(s).", totalDeleted);
        }

        log.info("Account cleanup completed.");
    }
}

