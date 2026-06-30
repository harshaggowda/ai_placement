package com.careeros.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * API Gateway — the single public entry point to the CareerOS AI platform.
 *
 * <p>Built on the reactive Spring Cloud Gateway. Responsibilities: routing requests to the
 * downstream services, forwarding authentication, and (future) centralized rate limiting and
 * request shaping. Routing is declared in {@code application.yml} so routes are configuration, not
 * code.
 *
 * <p>Deliberately isolated from {@code careeros-common} (which is servlet/Web-MVC based) to avoid
 * mixing the reactive and servlet stacks.
 */
@SpringBootApplication
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
