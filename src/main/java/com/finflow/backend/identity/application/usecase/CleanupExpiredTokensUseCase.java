package com.finflow.backend.identity.application.usecase;

import com.finflow.backend.identity.application.port.in.CleanupExpiredTokensPort;
import com.finflow.backend.identity.domain.repository.InvalidatedTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Component
@RequiredArgsConstructor
@Slf4j
public class CleanupExpiredTokensUseCase implements CleanupExpiredTokensPort {

    private final InvalidatedTokenRepository invalidatedTokenRepository;

    /**
     * Remove all invalidated tokens whose expiry time is before "now".
     * Intended to be invoked by a scheduler at off-peak hours.
     */
    @Transactional
    @Override
    public void execute() {
        long startTime = System.currentTimeMillis();
        Date now = new Date();

        try {
            log.info("[TokenCleanup] Starting cleanup of expired tokens before {}", now);

            // Count before deletion for metrics
            long expiredCount = invalidatedTokenRepository.countByExpiryTimeBefore(now);

            if (expiredCount == 0) {
                log.info("[TokenCleanup] No expired tokens to clean up");
                return;
            }

            // Delete expired tokens
            invalidatedTokenRepository.deleteByExpiryTimeBefore(now);

            long executionTime = System.currentTimeMillis() - startTime;

            log.info("[TokenCleanup] Successfully cleaned up {} expired tokens in {}ms",
                    expiredCount, executionTime);

            // Simple warning metric if taking too long
            if (executionTime > 5000) {
                log.warn("[TokenCleanup] Cleanup took longer than expected: {}ms", executionTime);
            }

        } catch (Exception e) {
            log.error("[TokenCleanup] Failed to cleanup expired tokens: {}", e.getMessage(), e);
            // Transaction will rollback automatically due to @Transactional
            throw e;  // Re-throw to ensure Spring sees the failure
        }
    }
}

