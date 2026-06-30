package com.careeros.payment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Payment Service — Stripe subscriptions, billing, webhook processing, and premium plans. Owns its own PostgreSQL database.
 *
 * <p>Independent, deployable microservice that owns its own PostgreSQL database. The platform
 * baseline (security, exception handling, logging, auditing, OpenAPI) is inherited from the
 * {@code careeros-common} library via component scanning of {@code com.careeros}.
 *
 * <p>This is an architectural skeleton: no controllers, business logic, or persistence are
 * implemented yet.
 */
@SpringBootApplication(scanBasePackages = "com.careeros")
public class PaymentServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaymentServiceApplication.class, args);
    }
}
