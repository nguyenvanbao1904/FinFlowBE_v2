package com.finflow.backend.identity.infrastructure.configuration;

import com.finflow.backend.common.exception.AppException;
import com.finflow.backend.common.exception.CommonErrorCode;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class InternalApiKeyFilter extends OncePerRequestFilter {

    private static final String INTERNAL_PATH_PATTERN = "/api/internal/**";
    private static final String INTERNAL_API_KEY_HEADER = "X-Internal-Api-Key";
    private static final String INTERNAL_SERVICE_ROLE = "ROLE_INTERNAL_SERVICE";

    private final InternalApiKeyProperties internalApiKeyProperties;
    @Qualifier("handlerExceptionResolver")
    private final HandlerExceptionResolver handlerExceptionResolver;
    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

    /** Fail-fast at startup: an empty internal API key is a misconfiguration. */
    @PostConstruct
    public void validateApiKey() {
        String key = internalApiKeyProperties.getKey();
        if (key == null || key.isBlank()) {
            throw new AppException(CommonErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String path = request.getRequestURI();
        if (!antPathMatcher.match(INTERNAL_PATH_PATTERN, path)) {
            filterChain.doFilter(request, response);
            return;
        }

        String expectedApiKey = internalApiKeyProperties.getKey();
        String providedApiKey = request.getHeader(INTERNAL_API_KEY_HEADER);

        if (!secureEquals(expectedApiKey, providedApiKey)) {
            handlerExceptionResolver.resolveException(
                    request,
                    response,
                    null,
                    new AppException(CommonErrorCode.UNAUTHENTICATED)
            );
            return;
        }

        // Set authenticated principal with ROLE_INTERNAL_SERVICE so that
        // SecurityFilterChain can enforce hasRole("INTERNAL_SERVICE") instead of permitAll().
        var auth = new UsernamePasswordAuthenticationToken(
                "internal-service", null,
                List.of(new SimpleGrantedAuthority(INTERNAL_SERVICE_ROLE))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        filterChain.doFilter(request, response);
    }

    private boolean secureEquals(String expected, String provided) {
        if (provided == null) {
            return false;
        }
        byte[] expectedBytes = expected.getBytes(StandardCharsets.UTF_8);
        byte[] providedBytes = provided.getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(expectedBytes, providedBytes);
    }
}
