package com.careeros.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.TimeZone;

/**
 * Centralized, platform-wide Jackson configuration.
 *
 * <p>Single programmatic source of truth for JSON (de)serialization across every service, so all
 * APIs behave identically: {@code null} fields omitted, ISO-8601 dates (never numeric timestamps),
 * UTC timezone, and tolerance of unknown inbound properties for forward compatibility. Implemented
 * as a {@link Jackson2ObjectMapperBuilderCustomizer} so Spring Boot's auto-configured
 * {@code ObjectMapper} (with the registered {@code JavaTimeModule}) is refined rather than replaced.
 */
@Configuration
public class JacksonConfig {

    @Bean
    Jackson2ObjectMapperBuilderCustomizer careerosJacksonCustomizer() {
        return builder -> builder
                .serializationInclusion(JsonInclude.Include.NON_NULL)
                .timeZone(TimeZone.getTimeZone("UTC"))
                .featuresToDisable(
                        SerializationFeature.WRITE_DATES_AS_TIMESTAMPS,
                        DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }
}
