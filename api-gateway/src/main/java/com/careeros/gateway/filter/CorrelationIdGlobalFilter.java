package com.careeros.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Global filter that establishes a correlation (trace) id at the edge and propagates it downstream.
 *
 * <p>Runs for every routed request: it honours an inbound {@code X-Trace-Id} or mints one, forwards
 * it on the downstream request, echoes it on the response, and logs one line per request. Because
 * the same header is read by each service's {@code CorrelationIdFilter}, a single id ties the edge
 * and all downstream logs together — the foundation for distributed tracing.
 */
@Component
public class CorrelationIdGlobalFilter implements GlobalFilter, Ordered {

    public static final String TRACE_ID_HEADER = "X-Trace-Id";

    private static final Logger log = LoggerFactory.getLogger(CorrelationIdGlobalFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String inbound = exchange.getRequest().getHeaders().getFirst(TRACE_ID_HEADER);
        String traceId = StringUtils.hasText(inbound) ? inbound : UUID.randomUUID().toString();

        ServerWebExchange mutated = exchange.mutate()
                .request(request -> request.headers(headers -> headers.set(TRACE_ID_HEADER, traceId)))
                .build();
        mutated.getResponse().getHeaders().set(TRACE_ID_HEADER, traceId);

        if (log.isDebugEnabled()) {
            log.debug("→ gateway {} {} [{}]",
                    exchange.getRequest().getMethod(), exchange.getRequest().getPath(), traceId);
        }
        return chain.filter(mutated);
    }

    /** Highest precedence so the trace id is present for every other filter. */
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
