package com.careeros.auth.entity;

/**
 * Supported external identity providers for social / federated login.
 *
 * <p>Persisted as a string. The actual OAuth2 flow is intentionally not implemented in this phase —
 * this only models which providers an account can be linked to.
 */
public enum OAuthProviderType {
    GOOGLE,
    GITHUB,
    LINKEDIN
}
