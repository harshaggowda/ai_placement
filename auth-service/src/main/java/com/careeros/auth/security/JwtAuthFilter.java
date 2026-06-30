package com.careeros.auth.security;

import com.careeros.security.JwtProperties;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 * Authenticates requests bearing a valid JWT access token.
 *
 * <p>Stateless: it verifies the token's signature/issuer/expiry and populates the
 * {@link SecurityContextHolder} from the token's claims — no database lookup per request. Invalid or
 * absent tokens leave the context unauthenticated; the entry point then returns a 401 for protected
 * routes. Authorization decisions are made downstream from the authorities carried in the token.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);

    private final JwtService jwtService;
    private final JwtProperties jwtProperties;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = resolveToken(request);
        if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                Claims claims = jwtService.parseClaims(token);
                AuthUserPrincipal principal = AuthUserPrincipal.fromClaims(
                        UUID.fromString(claims.getSubject()),
                        claims.get(JwtService.CLAIM_EMAIL, String.class),
                        extractAuthorities(claims));

                var authentication = new UsernamePasswordAuthenticationToken(
                        principal, null, principal.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (Exception ex) {
                // Invalid/expired/malformed token — proceed unauthenticated; never throw from the filter.
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
        Object raw = claims.get(JwtService.CLAIM_AUTHORITIES);
        if (raw instanceof List<?> list) {
            return list.stream().map(Object::toString).map(SimpleGrantedAuthority::new).toList();
        }
        return List.of();
    }
}
