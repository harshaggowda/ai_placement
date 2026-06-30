package com.careeros.audit;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Resolves the current actor for JPA auditing ({@code @CreatedBy} / {@code @LastModifiedBy}).
 *
 * <p>Reads the authenticated principal from the Spring Security context when present and falls back
 * to {@link #SYSTEM} for unauthenticated or system-initiated writes (migrations, schedulers, seed
 * data). Once the auth module populates the security context with real principals, audited entities
 * will automatically record the acting user — no change required here.
 */
@Component
public class ApplicationAuditorAware implements AuditorAware<String> {

    public static final String SYSTEM = "system";
    public static final String ANONYMOUS = "anonymousUser";

    @Override
    public Optional<String> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || ANONYMOUS.equals(authentication.getName())) {
            return Optional.of(SYSTEM);
        }
        return Optional.of(authentication.getName());
    }
}
