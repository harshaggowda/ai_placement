package com.careeros.security;

import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.UUID;

/**
 * Immutable principal placed in the security context for a request authenticated by a verified JWT.
 *
 * <p>Carries the platform user id (the auth-service user id, JWT {@code sub}), email, and granted
 * authorities. Resource services resolve the caller from this — e.g. for ownership checks — without
 * any database lookup. Issued by {@link JwtAuthenticationFilter}; exposed to controllers via
 * {@code @AuthenticationPrincipal} (or a {@code @CurrentUser} resolver).
 */
public record AuthenticatedUser(
        UUID userId,
        String email,
        Collection<? extends GrantedAuthority> authorities) {
}
