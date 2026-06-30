package com.careeros.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Activates Spring Data JPA auditing so {@code AuditableEntity} timestamps and actors populate
 * automatically. The {@code AuditorAware} bean is provided by {@code ApplicationAuditorAware}.
 *
 * <p>Guarded by {@link ConditionalOnClass} so that data-less services which reuse this shared
 * library (e.g. the API/AI gateways, which exclude Spring Data JPA) start cleanly without a
 * datasource. The condition is evaluated from class metadata, so the configuration is skipped
 * entirely — never loaded — when JPA is absent.
 */
@Configuration
@ConditionalOnClass(name = "jakarta.persistence.EntityManagerFactory")
@EnableJpaAuditing(auditorAwareRef = "applicationAuditorAware")
public class JpaAuditingConfig {
}
