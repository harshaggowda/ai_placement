package com.careeros.career;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Career Service — resume upload, interview history, progress and company tracking, analytics, LeetCode and SQL progress. Owns its own PostgreSQL database.
 *
 * <p>Independent, deployable microservice that owns its own PostgreSQL database. The platform
 * baseline (security, exception handling, logging, auditing, OpenAPI) is inherited from the
 * {@code careeros-common} library via component scanning of {@code com.careeros}.
 *
 * <p>This is an architectural skeleton: no controllers, business logic, or persistence are
 * implemented yet.
 */
@SpringBootApplication(scanBasePackages = "com.careeros")
public class CareerServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CareerServiceApplication.class, args);
    }
}
