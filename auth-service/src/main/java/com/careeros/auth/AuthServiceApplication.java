package com.careeros.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Auth Service — registration, login, JWT issuance and refresh tokens, RBAC, OAuth (future). Owns its own PostgreSQL database.
 *
 * <p>Independent, deployable microservice that owns its own PostgreSQL database. The platform
 * baseline (security, exception handling, logging, auditing, OpenAPI) is inherited from the
 * {@code careeros-common} library via component scanning of {@code com.careeros}.
 *
 * <p>This is an architectural skeleton: no controllers, business logic, or persistence are
 * implemented yet.
 */
@SpringBootApplication(scanBasePackages = "com.careeros")
public class AuthServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }
}
