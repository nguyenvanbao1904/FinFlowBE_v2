package com.finflow.backend.modules.identity.infrastructure.scheduler;

import com.finflow.backend.modules.identity.application.usecase.CleanupDeletedAccountsUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserCleanupScheduler {

    private final CleanupDeletedAccountsUseCase cleanupDeletedAccountsUseCase;

    // Run every day at midnight
    @Scheduled(cron = "0 0 0 * * ?")
    public void cleanupDeletedAccounts() {
        log.info("Triggering scheduled account cleanup job...");
        cleanupDeletedAccountsUseCase.execute();
    }
}
