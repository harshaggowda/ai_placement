package com.careeros.user;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Abstract base that provides a single, shared Testcontainers PostgreSQL instance.
 *
 * <p>Reuse the same container across all tests in a class hierarchy via Testcontainers'
 * static container lifecycle. Flyway runs inside the container on every context load,
 * ensuring the schema is always consistent with production migrations.
 */
@Testcontainers
public abstract class PostgresContainerBase {

    @Container
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("careeros_user_test")
                    .withUsername("test")
                    .withPassword("test");

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        // Disable Redis autoconfiguration for tests that don't need it.
        registry.add("spring.autoconfigure.exclude",
                () -> "org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration," +
                      "org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration");
    }
}
