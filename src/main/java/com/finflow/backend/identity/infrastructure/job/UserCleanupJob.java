package com.finflow.backend.identity.infrastructure.job;

import com.finflow.backend.identity.application.port.in.CleanupDeletedAccountsPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserCleanupJob {

    private final CleanupDeletedAccountsPort cleanupDeletedAccountsPort;

    @Scheduled(cron = "0 0 0 * * ?")
    public void cleanupDeletedAccounts() {
        log.info("Triggering scheduled account cleanup job...");
        cleanupDeletedAccountsPort.execute();
    }
}
