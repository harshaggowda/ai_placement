package com.careeros.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Central OpenAPI / Swagger UI definition.
 *
 * <p>Declares the platform metadata and a single {@code bearer-jwt} security scheme so that, once the
 * auth module is implemented, every protected endpoint can require a JWT through the Swagger
 * "Authorize" dialog. Swagger UI is served at {@code /swagger-ui.html} and the spec at
 * {@code /v3/api-docs} (both exposed only in non-prod profiles via configuration).
 */
@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "bearer-jwt";

    @Bean
    public OpenAPI careerOsOpenAPI(@Value("${server.servlet.context-path:}") String contextPath) {
        return new OpenAPI()
                .info(new Info()
                        .title("CareerOS AI API")
                        .description("AI Powered Career Operating System for Software Engineers")
                        .version("v0.1.0")
                        .contact(new Contact().name("CareerOS AI").email("engineering@careeros.ai"))
                        .license(new License().name("Proprietary")))
                .servers(List.of(new Server().url(contextPath == null ? "" : contextPath)
                        .description("Current environment")))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components().addSecuritySchemes(SECURITY_SCHEME_NAME,
                        new SecurityScheme()
                                .name(SECURITY_SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Provide a valid JWT access token issued by the auth module")));
    }
}
