package com.finflow.backend.modules.identity.infrastructure.service;

import com.finflow.backend.modules.identity.domain.repository.InvalidatedTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenCleanupService {

    private final InvalidatedTokenRepository invalidatedTokenRepository;

    @Scheduled(cron = "0 0 2 * * *")  // 2 AM to avoid peak hours
    @Transactional
    public void cleanupExpiredTokens() {
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
            
            // ✅ Add metrics for monitoring (can be extended with Micrometer)
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