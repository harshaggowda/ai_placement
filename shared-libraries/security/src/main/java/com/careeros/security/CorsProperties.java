package com.careeros.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Externalized CORS policy bound from {@code careeros.security.cors.*} so that allowed origins can
 * differ per environment (permissive in dev, locked-down in prod) without code changes.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "careeros.security.cors")
public class CorsProperties {

    private List<String> allowedOrigins = List.of("http://localhost:3000");
    private List<String> allowedMethods = List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS");
    private List<String> allowedHeaders = List.of("*");
    private List<String> exposedHeaders = List.of("X-Trace-Id");
    private boolean allowCredentials = true;
    private long maxAge = 3600;
}
