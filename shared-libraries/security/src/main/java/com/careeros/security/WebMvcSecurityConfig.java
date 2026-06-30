package com.careeros.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * Registers the {@link CurrentUserArgumentResolver} for all servlet services, enabling
 * {@code @CurrentUser AuthenticatedUser} injection in controllers platform-wide.
 */
@Configuration
public class WebMvcSecurityConfig implements WebMvcConfigurer {

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new CurrentUserArgumentResolver());
    }
}
