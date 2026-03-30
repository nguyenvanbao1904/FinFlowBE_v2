package com.finflow.backend.investment.portfolio.application.service;

import com.finflow.backend.investment.portfolio.application.exception.SnapshotDataNotReadyException;
import com.finflow.backend.investment.portfolio.application.usecase.RecordDailyPortfolioSnapshotsUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.ExhaustedRetryException;
import org.springframework.retry.RetryContext;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;

/**
 * Retry VNINDEX thiếu dữ liệu: tối đa 5 lần, cách 30 phút (tương đương {@code @Retryable} trong plan).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DailyPortfolioSnapshotRetryService {

    private static final int MAX_ATTEMPTS = 5;
    private static final long BACKOFF_MS = 1_800_000L;

    private final RecordDailyPortfolioSnapshotsUseCase recordDailyPortfolioSnapshotsUseCase;

    private final RetryTemplate retryTemplate = RetryTemplate.builder()
            .maxAttempts(MAX_ATTEMPTS)
            .fixedBackoff(Duration.ofMillis(BACKOFF_MS))
            .retryOn(SnapshotDataNotReadyException.class)
            .withListener(new org.springframework.retry.RetryListener() {
                @Override
                public <T, E extends Throwable> void onError(
                        RetryContext context,
                        org.springframework.retry.RetryCallback<T, E> callback,
                        Throwable throwable
                ) {
                    if (throwable instanceof SnapshotDataNotReadyException ex) {
                        log.warn(
                                "Daily snapshot attempt {}/{} failed (will retry if attempts remain): {}",
                                context.getRetryCount(),
                                MAX_ATTEMPTS,
                                ex.getMessage()
                        );
                    }
                }
            })
            .build();

    public void recordForTradingDay(LocalDate snapshotDate) {
        try {
            retryTemplate.execute(ctx -> {
                recordDailyPortfolioSnapshotsUseCase.execute(snapshotDate);
                return null;
            });
        } catch (ExhaustedRetryException ex) {
            String msg = ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage();
            log.error("Daily portfolio snapshot job gave up for {} after {} attempts: {}", snapshotDate, MAX_ATTEMPTS, msg);
        }
    }
}
