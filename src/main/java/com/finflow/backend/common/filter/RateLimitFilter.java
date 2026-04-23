package com.finflow.backend.common.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Simple in-memory IP-based rate limiter for auth endpoints.
 * <p>
 * Limits per minute per IP:
 * <ul>
 *   <li>/api/auth/login — 10 req/min</li>
 *   <li>/api/auth/register — 5 req/min</li>
 *   <li>/api/auth/send-otp — 5 req/min</li>
 *   <li>/api/auth/check-user-existence — 10 req/min</li>
 *   <li>/api/auth/** (other) — 20 req/min</li>
 * </ul>
 * <p>
 * Uses {@code request.getRemoteAddr()} only — does NOT trust X-Forwarded-For to prevent
 * IP spoofing. If deployed behind a trusted proxy, configure
 * {@code server.forward-headers-strategy=framework} so Spring rewrites remoteAddr automatically.
 * <p>
 * For production at scale, replace with Bucket4j + Redis or an API gateway rate limiter.
 */
@Component
@Order(1)
@Slf4j
public class RateLimitFilter extends OncePerRequestFilter {

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    /** path pattern → max requests per window */
    private static final Map<String, Integer> RATE_LIMITS = Map.of(
            "/api/auth/login", 10,
            "/api/auth/register", 5,
            "/api/auth/send-otp", 5,
            "/api/auth/check-user-existence", 10
    );
    private static final int DEFAULT_AUTH_LIMIT = 20;
    private static final long WINDOW_MS = 60_000; // 1 minute

    /** key = "ip:path" → bucket */
    private final ConcurrentHashMap<String, RateBucket> buckets = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();

        if (!path.startsWith("/api/auth/")) {
            filterChain.doFilter(request, response);
            return;
        }

        int limit = RATE_LIMITS.entrySet().stream()
                .filter(e -> PATH_MATCHER.match(e.getKey(), path))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(DEFAULT_AUTH_LIMIT);

        String ip = resolveClientIp(request);
        String bucketKey = ip + ":" + path;

        RateBucket bucket = buckets.compute(bucketKey, (k, existing) -> {
            long now = System.currentTimeMillis();
            if (existing == null || now - existing.windowStart > WINDOW_MS) {
                return new RateBucket(now, new AtomicInteger(0));
            }
            return existing;
        });

        if (bucket.counter.incrementAndGet() > limit) {
            log.warn("Rate limit exceeded: ip={} path={} limit={}/min", ip, path, limit);
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write(
                    "{\"code\":429,\"message\":\"Too many requests. Please try again later.\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    /** Evict expired buckets every 2 minutes to prevent unbounded memory growth. */
    @Scheduled(fixedDelay = 120_000)
    public void evictExpiredBuckets() {
        long now = System.currentTimeMillis();
        buckets.entrySet().removeIf(e -> now - e.getValue().windowStart > WINDOW_MS * 2);
    }

    /**
     * Use {@code request.getRemoteAddr()} only.
     * Do NOT trust X-Forwarded-For — it can be spoofed by clients.
     * If behind a trusted proxy, configure {@code server.forward-headers-strategy=framework}
     * so Spring sets remoteAddr from the proxy header automatically.
     */
    private String resolveClientIp(HttpServletRequest request) {
        return request.getRemoteAddr();
    }

    private record RateBucket(long windowStart, AtomicInteger counter) {}
}
