package com.careeros.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Establishes a per-request correlation id (trace id) and publishes it to the SLF4J {@link MDC} and
 * the response headers.
 *
 * <p>An inbound {@code X-Trace-Id} (e.g. propagated by an API gateway) is honoured; otherwise a new
 * id is minted. Every log line emitted during the request — and every error envelope returned by
 * {@code GlobalExceptionHandler} — carries this id, making distributed debugging tractable. The MDC
 * is always cleared in a {@code finally} block to avoid leaking ids across pooled threads.
 *
 * <p>Runs first in the filter chain so the trace id is available to all downstream filters.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationIdFilter extends OncePerRequestFilter {

    public static final String TRACE_ID_KEY = "traceId";
    public static final String TRACE_ID_HEADER = "X-Trace-Id";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String traceId = resolveTraceId(request);
        MDC.put(TRACE_ID_KEY, traceId);
        response.setHeader(TRACE_ID_HEADER, traceId);
        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(TRACE_ID_KEY);
        }
    }

    private String resolveTraceId(HttpServletRequest request) {
        String inbound = request.getHeader(TRACE_ID_HEADER);
        return StringUtils.hasText(inbound) ? inbound : UUID.randomUUID().toString();
    }
}
