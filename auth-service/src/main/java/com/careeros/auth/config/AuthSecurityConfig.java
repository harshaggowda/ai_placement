package com.careeros.auth.config;

import com.careeros.auth.security.CustomUserDetailsService;
import com.careeros.auth.security.JwtAuthFilter;
import com.careeros.security.RestAccessDeniedHandler;
import com.careeros.security.RestAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

/**
 * Auth-service security: a real, stateless JWT chain that overrides the shared permissive baseline
 * (which backs off via {@code @ConditionalOnMissingBean}).
 *
 * <p>Reuses platform building blocks from {@code careeros-security} — the BCrypt {@link PasswordEncoder},
 * CORS source, and JSON 401/403 handlers — and adds the {@link JwtAuthFilter}, the authentication
 * manager, and the authorization matrix. Method security ({@code @EnableMethodSecurity}) is already
 * enabled by the shared config.
 */
@Configuration
@RequiredArgsConstructor
public class AuthSecurityConfig {

    /** Endpoints reachable without authentication. */
    private static final String[] PUBLIC_GET = {
            "/api/auth/verify-email",
            "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html",
            "/actuator/health/**", "/actuator/info", "/error"
    };
    private static final String[] PUBLIC_POST = {
            "/api/auth/register", "/api/auth/login", "/api/auth/refresh",
            "/api/auth/forgot-password", "/api/auth/reset-password"
    };

    private final JwtAuthFilter jwtAuthFilter;
    private final RestAuthenticationEntryPoint authenticationEntryPoint;
    private final RestAccessDeniedHandler accessDeniedHandler;
    private final CorsConfigurationSource corsConfigurationSource;

    @Bean
    public SecurityFilterChain authSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(handling -> handling
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(HttpMethod.GET, PUBLIC_GET).permitAll()
                        .requestMatchers(HttpMethod.POST, PUBLIC_POST).permitAll()
                        .requestMatchers("/api/auth/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Authentication manager backed by the DB-loaded user details and the platform password encoder.
     * Used by the login flow to verify credentials.
     */
    @Bean
    public AuthenticationManager authenticationManager(CustomUserDetailsService userDetailsService,
                                                       PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(provider);
    }
}
