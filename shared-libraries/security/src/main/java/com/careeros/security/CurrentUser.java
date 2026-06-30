package com.careeros.security;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Injects the authenticated {@link AuthenticatedUser} into a controller method parameter.
 *
 * <p>Resolved by {@link CurrentUserArgumentResolver}. Cleaner than {@code @AuthenticationPrincipal}
 * and consistent across services: {@code public X handler(@CurrentUser AuthenticatedUser user)}.
 */
@Documented
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface CurrentUser {
}
