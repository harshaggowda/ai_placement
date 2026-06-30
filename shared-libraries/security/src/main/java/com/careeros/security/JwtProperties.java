package com.careeros.security;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

/**
 * Strongly-typed JWT configuration bound from {@code careeros.security.jwt.*}.
 *
 * <p>This is the configuration surface of the security skeleton. Token <em>issuance and
 * verification</em> are intentionally not implemented here — they will be owned by the auth module,
 * which will consume these properties. Keeping the contract in place now means the auth module can
 * be added later without touching application configuration or deployment manifests.
 *
 * <p>The {@code secret} must be supplied via environment/secret manager in every non-local
 * environment; never commit a production signing key.
 */
@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "careeros.security.jwt")
public class JwtProperties {

    /** HMAC signing secret (Base64). Injected from the environment in dev/prod. */
    @NotBlank
    private String secret;

    /** Token issuer claim ({@code iss}). */
    @NotBlank
    private String issuer = "careeros-ai";

    /** Lifetime of access tokens. */
    @NotNull
    private Duration accessTokenExpiration = Duration.ofMinutes(15);

    /** Lifetime of refresh tokens. */
    @NotNull
    private Duration refreshTokenExpiration = Duration.ofDays(7);

    /** Authorization header carrying the bearer token. */
    @NotBlank
    private String headerName = "Authorization";

    /** Prefix stripped from the header value before parsing the token. */
    @NotBlank
    private String tokenPrefix = "Bearer ";
}
