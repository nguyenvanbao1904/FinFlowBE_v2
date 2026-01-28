package com.finflow.backend.common.versioning;

import com.finflow.backend.common.exception.AppException;
import com.finflow.backend.common.exception.CommonErrorCode;
import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Set;

/**
 * Simple API version guard using header-based versioning.
 *
 * - Header: API-Version
 * - Default: 1 (if header missing)
 * - If version not in supported set -> 400 with ProblemDetail (handled by GlobalExceptionHandler)
 */
@Component
public class ApiVersionInterceptor implements HandlerInterceptor {

    private static final String HEADER = "API-Version";
    private static final String ATTR = "apiVersion";
    private static final Set<String> SUPPORTED = Set.of("1"); // add "2" when a new version ships

    @Override
    @NullMarked
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String version = request.getHeader(HEADER);
        if (version == null || version.isBlank()) {
            version = "1";
        }

        if (!SUPPORTED.contains(version)) {
            throw new AppException(CommonErrorCode.UNSUPPORTED_API_VERSION);
        }

        request.setAttribute(ATTR, version);
        return true;
    }
}

