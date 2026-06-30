package com.careeros.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * Authenticates requests carrying a valid bearer JWT, for every resource service in the platform.
 *
 * <p>Stateless: it verifies the token via {@link JwtTokenValidator} and populates the security
 * context with an {@link AuthenticatedUser} built from the claims — no database lookup. Invalid or
 * absent tokens leave the context unauthenticated (the entry point then returns 401 for protected
 * routes). Registered as a bean but <strong>excluded from servlet auto-registration</strong> (see
 * {@code SecurityConfig}); it runs only where a {@code SecurityFilterChain} adds it explicitly.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenValidator tokenValidator;
    private final JwtProperties jwtProperties;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = resolveToken(request);
        if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                Claims claims = tokenValidator.validate(token);
                AuthenticatedUser principal = new AuthenticatedUser(
                        UUID.fromString(claims.getSubject()),
                        claims.get(JwtTokenValidator.CLAIM_EMAIL, String.class),
                        extractAuthorities(claims));

                var authentication = new UsernamePasswordAuthenticationToken(
                        principal, null, principal.authorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (Exception ex) {
                log.debug("Rejected JWT on {}: {}", request.getRequestURI(), ex.getMessage());
                SecurityContextHolder.clearContext();
            }
        }
        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String header = request.getHeader(jwtProperties.getHeaderName());
        String prefix = jwtProperties.getTokenPrefix();
        if (StringUtils.hasText(header) && header.startsWith(prefix)) {
            return header.substring(prefix.length()).trim();
        }
        return null;
    }

    private List<SimpleGrantedAuthority> extractAuthorities(Claims claims) {
        Object raw = claims.get(JwtTokenValidator.CLAIM_AUTHORITIES);
        if (raw instanceof List<?> list) {
            return list.stream().map(Object::toString).map(SimpleGrantedAuthority::new).toList();
        }
        return List.of();
    }
}
