package com.careeros.aiorchestrator.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.support.RetryTemplate;

import java.time.Duration;

/**
 * Retry policy for transient failures when calling the FastAPI AI service.
 *
 * <p>Enables Spring Retry (so {@code @Retryable} can be used in the service layer) and exposes a
 * pre-tuned {@link RetryTemplate} sized from {@link AiServiceProperties#getMaxRetries()} with a fixed
 * backoff. Only idempotent downstream calls should use it. The set of retryable exceptions will be
 * narrowed (to timeouts / 5xx) once the {@code AiServiceClient} defines its error types.
 */
@Configuration
@EnableRetry
public class RetryConfig {

    private static final Duration BACKOFF = Duration.ofMillis(200);

    @Bean
    public RetryTemplate aiServiceRetryTemplate(AiServiceProperties properties) {
        return RetryTemplate.builder()
                .maxAttempts(properties.getMaxRetries() + 1)
                .fixedBackoff(BACKOFF.toMillis())
                .build();
    }
}
