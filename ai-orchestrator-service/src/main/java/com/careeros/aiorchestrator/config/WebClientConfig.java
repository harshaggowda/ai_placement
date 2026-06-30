package com.careeros.aiorchestrator.config;

import io.netty.channel.ChannelOption;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

/**
 * Reactive {@link WebClient} for non-blocking / streaming calls to the FastAPI AI service.
 *
 * <p>Complements {@link AiServiceClientConfig}'s blocking {@code RestClient}: this transport suits
 * token-streaming or long-running AI responses. Base URL and timeouts come from
 * {@link AiServiceProperties}. Only the transport is established here — request methods, auth-header
 * propagation, and error mapping belong to a future {@code AiServiceClient} in the service layer.
 */
@Configuration
public class WebClientConfig {

    @Bean
    public WebClient aiServiceWebClient(AiServiceProperties properties) {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) properties.getConnectTimeout().toMillis())
                .responseTimeout(properties.getReadTimeout());

        return WebClient.builder()
                .baseUrl(properties.getBaseUrl())
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
}
