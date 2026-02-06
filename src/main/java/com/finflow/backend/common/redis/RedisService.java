package com.finflow.backend.common.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
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
            throw new RuntimeException("Failed to write to Redis: " + e.getMessage(), e);
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
            throw new RuntimeException("Failed to read from Redis: " + e.getMessage(), e);
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
}
