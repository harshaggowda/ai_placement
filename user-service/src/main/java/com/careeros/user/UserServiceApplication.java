package com.careeros.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * User Service — profile, resume metadata, progress, goals, roadmaps, and dashboard data. Owns its own PostgreSQL database.
 *
 * <p>Independent, deployable microservice that owns its own PostgreSQL database. The platform
 * baseline (security, exception handling, logging, auditing, OpenAPI) is inherited from the
 * {@code careeros-common} library via component scanning of {@code com.careeros}.
 *
 * <p>This is an architectural skeleton: no controllers, business logic, or persistence are
 * implemented yet.
 */
@SpringBootApplication(scanBasePackages = "com.careeros")
public class UserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}
