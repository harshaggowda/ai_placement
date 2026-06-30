package com.careeros.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Logs one concise line per completed HTTP request (status + path + elapsed ms) at DEBUG.
 *
 * <p>Wraps the remainder of the filter chain to measure end-to-end latency, logging in a
 * {@code finally} block so the outcome is recorded even when the request fails. Like
 * {@link RequestLoggingFilter} it never touches the response body — it reports the status line and
 * timing only.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
public class ResponseLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(ResponseLoggingFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        long startNanos = System.nanoTime();
        try {
            filterChain.doFilter(request, response);
        } finally {
            if (log.isDebugEnabled()) {
                long elapsedMs = (System.nanoTime() - startNanos) / 1_000_000;
                log.debug("← {} {} ({}ms)", response.getStatus(), request.getRequestURI(), elapsedMs);
            }
        }
    }
}
