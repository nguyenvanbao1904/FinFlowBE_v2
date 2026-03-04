package com.finflow.backend.common.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

/**
 * Servlet filter that ensures every incoming HTTP request has a correlationId
 * stored in MDC so that all logs for that request share the same identifier.
 */
@Component
@Order(1)
public class CorrelationIdFilter implements Filter {

    public static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    public static final String MDC_KEY = "correlationId";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        if (request instanceof HttpServletRequest httpRequest) {
            String correlationId = Optional.ofNullable(httpRequest.getHeader(CORRELATION_ID_HEADER))
                    .filter(id -> !id.isBlank())
                    .orElseGet(() -> UUID.randomUUID().toString());

            MDC.put(MDC_KEY, correlationId);
        }

        try {
            chain.doFilter(request, response);
        } finally {
            MDC.remove(MDC_KEY);
        }
    }
}

