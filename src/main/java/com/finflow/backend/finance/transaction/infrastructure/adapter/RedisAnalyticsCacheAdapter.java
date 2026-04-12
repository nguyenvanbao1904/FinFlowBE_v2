package com.finflow.backend.finance.transaction.infrastructure.adapter;

import com.finflow.backend.common.redis.RedisService;
import com.finflow.backend.finance.transaction.application.port.out.AnalyticsCachePort;
import com.finflow.backend.finance.transaction.application.result.AnalyticsInsightsResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Redis-backed implementation of {@link AnalyticsCachePort}.
 * Lives in infrastructure so that the use case depends only on the port interface.
 */
@Component
@RequiredArgsConstructor
public class RedisAnalyticsCacheAdapter implements AnalyticsCachePort {

    private final RedisService redisService;

    @Override
    public Optional<AnalyticsInsightsResult> get(String cacheKey) {
        AnalyticsInsightsResult cached = redisService.getSilently(cacheKey, AnalyticsInsightsResult.class);
        if (cached != null && cached.insights() != null && !cached.insights().isEmpty()) {
            return Optional.of(new AnalyticsInsightsResult(cached.insights(), true));
        }
        return Optional.empty();
    }

    @Override
    public void put(String cacheKey, AnalyticsInsightsResult result, long ttl, TimeUnit unit) {
        redisService.setSilently(cacheKey, result, ttl, unit);
    }
}
