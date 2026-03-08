package com.finflow.backend.identity.infrastructure.service;

import com.finflow.backend.identity.application.usecase.CleanupExpiredTokensUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenCleanupService {

    private final CleanupExpiredTokensUseCase cleanupExpiredTokensUseCase;

    // 2 AM to avoid peak hours
    @Scheduled(cron = "0 0 2 * * *")
    public void cleanupExpiredTokens() {
        cleanupExpiredTokensUseCase.execute();
    }
}
