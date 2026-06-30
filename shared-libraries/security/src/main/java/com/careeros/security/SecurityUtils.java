package com.careeros.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/**
 * Static helpers for reading the current {@link Authentication} from the security context.
 *
 * <p>Gives services a single, null-safe way to obtain the acting principal without touching
 * {@link SecurityContextHolder} directly. Mirrors the resolution rules used by JPA auditing
 * (anonymous/unauthenticated requests yield {@link Optional#empty()}). Read-only — it never mutates
 * the context. Token validation that populates the context lands with the auth module.
 */
public final class SecurityUtils {

    /** Principal name Spring Security uses for unauthenticated requests. */
    public static final String ANONYMOUS = "anonymousUser";

    private SecurityUtils() {
        throw new AssertionError("No com.careeros.security.SecurityUtils instances for you!");
    }

    /** The current authentication, if one is present and authenticated (not anonymous). */
    public static Optional<Authentication> getAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || ANONYMOUS.equals(authentication.getName())) {
            return Optional.empty();
        }
        return Optional.of(authentication);
    }

    /** The current authenticated principal's name, if any. */
    public static Optional<String> getCurrentUsername() {
        return getAuthentication().map(Authentication::getName);
    }

    /** Whether the current request is authenticated by a non-anonymous principal. */
    public static boolean isAuthenticated() {
        return getAuthentication().isPresent();
    }
}
