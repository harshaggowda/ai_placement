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
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Logs one concise line per inbound HTTP request (method + path + query) at DEBUG.
 *
 * <p>Runs just after {@link CorrelationIdFilter}, so every line carries the request's trace id via
 * the MDC. Deliberately does not read or buffer the request body — payload logging is intrusive and
 * a security risk; this filter is a lightweight access trace only. Paired with
 * {@link ResponseLoggingFilter}, which records the outcome and latency.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (log.isDebugEnabled()) {
            String query = request.getQueryString();
            log.debug("→ {} {}{}", request.getMethod(), request.getRequestURI(),
                    StringUtils.hasText(query) ? "?" + query : "");
        }
        filterChain.doFilter(request, response);
    }
}
