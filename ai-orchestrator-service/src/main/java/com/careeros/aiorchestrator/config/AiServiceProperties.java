package com.careeros.aiorchestrator.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * Connection contract for the downstream FastAPI AI service, bound from {@code careeros.ai-service.*}.
 *
 * <p>Centralizes the bridge's resilience knobs (base URL, timeouts, retry budget) so they can be
 * tuned per environment without code changes. Consumed by the (future) {@code RestClient}/HTTP
 * client wiring; declared now so the integration boundary is explicit and configurable.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "careeros.ai-service")
public class AiServiceProperties {

    /** Base URL of the FastAPI AI service (e.g. http://ai-service:8000). */
    private String baseUrl = "http://localhost:8000";

    /** TCP connect timeout for calls to the AI service. */
    private Duration connectTimeout = Duration.ofSeconds(2);

    /** Read timeout — AI calls can be slow; sized generously. */
    private Duration readTimeout = Duration.ofSeconds(60);

    /** Maximum retry attempts for transient/idempotent failures. */
    private int maxRetries = 2;

    /** Shared secret / API key forwarded to the AI service for service-to-service auth. */
    private String apiKey = "";
}
