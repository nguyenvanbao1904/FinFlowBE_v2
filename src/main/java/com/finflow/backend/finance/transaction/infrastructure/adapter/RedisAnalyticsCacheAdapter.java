package com.finflow.backend.finance.transaction.infrastructure.adapter;

import com.finflow.backend.common.infrastructure.redis.RedisService;
import com.finflow.backend.finance.transaction.application.dto.AnalyticsInsightsOutput;
import com.finflow.backend.finance.transaction.application.port.out.AnalyticsCachePort;
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
    public Optional<AnalyticsInsightsOutput> get(String cacheKey) {
        AnalyticsInsightsOutput cached = redisService.getSilently(cacheKey, AnalyticsInsightsOutput.class);
        if (cached != null && cached.insights() != null && !cached.insights().isEmpty()) {
            return Optional.of(new AnalyticsInsightsOutput(cached.insights(), true));
        }
        return Optional.empty();
    }

    @Override
    public void put(String cacheKey, AnalyticsInsightsOutput result, long ttl, TimeUnit unit) {
        redisService.setSilently(cacheKey, result, ttl, unit);
    }
}
