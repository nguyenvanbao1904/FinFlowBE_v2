package com.finflow.backend.common.infrastructure.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finflow.backend.common.exception.AppException;
import com.finflow.backend.common.exception.CommonErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisService {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    public <T> void set(String key, T value, long timeout, TimeUnit unit) {
        try {
            String json = objectMapper.writeValueAsString(value);
            redisTemplate.opsForValue().set(key, json, timeout, unit);
            log.debug("Set Redis key: {} with TTL: {} {}", key, timeout, unit);
        } catch (Exception e) {
            log.error("Error writing to Redis for key: {}", key, e);
            throw new AppException(CommonErrorCode.REDIS_WRITE_ERROR);
        }
    }

    public <T> T get(String key, Class<T> targetClass) {
        String json = redisTemplate.opsForValue().get(key);
        if (json == null) {
            log.debug("Redis key not found: {}", key);
            return null;
        }

        try {
            return objectMapper.readValue(json, targetClass);
        } catch (Exception e) {
            log.error("Error reading from Redis for key: {}", key, e);
            throw new AppException(CommonErrorCode.REDIS_READ_ERROR);
        }
    }

    public boolean delete(String key) {
        Boolean result = redisTemplate.delete(key);
        log.debug("Deleted Redis key: {} - Result: {}", key, result);
        return Boolean.TRUE.equals(result);
    }

    public boolean exists(String key) {
        Boolean result = redisTemplate.hasKey(key);
        return Boolean.TRUE.equals(result);
    }

    /**
     * Cache read that never throws - returns null on miss or parse error (fail-open).
     */
    public <T> T getSilently(String key, Class<T> targetClass) {
        try {
            String json = redisTemplate.opsForValue().get(key);
            if (json == null) {
                return null;
            }
            return objectMapper.readValue(json, targetClass);
        } catch (Exception e) {
            log.warn("Redis silent get failed for key {}: {}", key, e.getMessage());
            return null;
        }
    }

    /**
     * Cache write that never throws - no-op on failure (fail-open).
     */
    public <T> void setSilently(String key, T value, long timeout, TimeUnit unit) {
        try {
            String json = objectMapper.writeValueAsString(value);
            redisTemplate.opsForValue().set(key, json, timeout, unit);
            log.debug("Redis silent set key {} TTL {} {}", key, timeout, unit);
        } catch (Exception e) {
            log.warn("Redis silent set failed for key {}: {}", key, e.getMessage());
        }
    }
}
