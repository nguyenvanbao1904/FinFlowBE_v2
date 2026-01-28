package com.finflow.backend.modules.identity;

import com.finflow.backend.modules.identity.domain.repository.InvalidatedTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * Infrastructure Service: Token Cleanup
 * 
 * WHY Infrastructure Service? ✅ VALID theo FEATURE_DEVELOPMENT_GUIDE:
 * - Scheduled task (infrastructure concern, not user-facing use case)
 * - Technical maintenance operation
 * - Framework integration (@Scheduled annotation)
 * 
 * Responsibilities:
 * - Cleanup expired invalidated tokens daily at midnight
 * - Prevent database bloat from accumulated blacklisted tokens
 * 
 * Pattern: @Scheduled Service → Repository
 * NOT a UseCase because users don't trigger this - it's system maintenance
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TokenCleanupService {

    private final InvalidatedTokenRepository invalidatedTokenRepository;

    /**
     * Cleanup expired tokens daily at midnight (00:00:00)
     * Cron format: second minute hour day month weekday
     */
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void cleanupExpiredTokens() {
        log.info("Starting cleanup of expired tokens...");

        Date now = new Date();
        invalidatedTokenRepository.deleteByExpiryTimeBefore(now);

        log.info("Completed cleanup of expired tokens before {}", now);
    }
}