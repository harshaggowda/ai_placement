package com.careeros.aiorchestrator.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/**
 * Builds the HTTP client used to reach the FastAPI AI service.
 *
 * <p>Provides a pre-configured {@link RestClient} with the base URL and timeouts from
 * {@link AiServiceProperties}. The actual request/response methods (calling the agents, mapping
 * payloads, applying retries) belong to a future {@code AiServiceClient} in the {@code service}
 * package — this configuration only establishes the transport, keeping the integration boundary
 * explicit and testable.
 */
@Configuration
@EnableConfigurationProperties(AiServiceProperties.class)
public class AiServiceClientConfig {

    @Bean
    public RestClient aiServiceRestClient(AiServiceProperties properties) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(properties.getConnectTimeout());
        requestFactory.setReadTimeout(properties.getReadTimeout());

        return RestClient.builder()
                .baseUrl(properties.getBaseUrl())
                .requestFactory(requestFactory)
                .build();
    }
}
