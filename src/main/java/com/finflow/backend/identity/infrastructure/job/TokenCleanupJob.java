package com.finflow.backend.identity.infrastructure.job;

import com.finflow.backend.identity.application.port.in.CleanupExpiredTokensPort;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TokenCleanupJob {

    private final CleanupExpiredTokensPort cleanupExpiredTokensPort;

    @Scheduled(cron = "0 0 2 * * *")
    public void cleanupExpiredTokens() {
        cleanupExpiredTokensPort.execute();
    }
}
