package com.careeros.auth;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Base for tests that need a real PostgreSQL, managed by the Testcontainers JUnit 5 extension.
 *
 * <p>{@code disabledWithoutDocker = true} makes these tests <em>skip</em> (not fail) on machines
 * without a running Docker daemon, while running normally in CI and any Docker-enabled environment.
 */
@Testcontainers(disabledWithoutDocker = true)
public abstract class AbstractPostgresContainerTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }
}
