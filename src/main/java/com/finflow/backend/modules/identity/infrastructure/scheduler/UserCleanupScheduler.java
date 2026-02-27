package com.finflow.backend.modules.identity.infrastructure.scheduler;

import com.finflow.backend.modules.identity.domain.entity.User;
import com.finflow.backend.modules.identity.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserCleanupScheduler {

    private final UserRepository userRepository;
    private final org.springframework.context.ApplicationEventPublisher eventPublisher;

    // Run every day at midnight
    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void cleanupDeletedAccounts() {
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
                // or just to notify.
                eventPublisher.publishEvent(new com.finflow.backend.modules.identity.application.event.AccountHardDeletedEvent(
                        user.getEmail(),
                        user.getUsername()
                ));
                
                log.info("Permanently deleting user: {} (Deleted at: {})", user.getUsername(), user.getDeletedAt());
                userRepository.delete(user);
            } catch (Exception e) {
                log.error("Failed to delete user {}: {}", user.getUsername(), e.getMessage());
            }
        }
        
        log.info("Account cleanup completed.");
    }
}
