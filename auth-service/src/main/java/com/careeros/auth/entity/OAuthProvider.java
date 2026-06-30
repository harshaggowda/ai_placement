package com.careeros.auth.entity;

import com.careeros.common.entity.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

/**
 * A link between a {@link User} and an external identity provider.
 *
 * <p>A given external identity ({@code provider} + {@code providerUserId}) maps to at most one
 * account, and an account links a provider at most once. The OAuth2 flow itself is out of scope.
 */
@Getter
@Setter
@Entity
@Table(
        name = "oauth_providers",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_oauth_provider_identity",
                        columnNames = {"provider", "provider_user_id"}),
                @UniqueConstraint(name = "uk_oauth_provider_user",
                        columnNames = {"user_id", "provider"})
        },
        indexes = @Index(name = "idx_oauth_providers_user", columnList = "user_id"))
public class OAuthProvider extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_oauth_providers_user"))
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false, length = 30)
    private OAuthProviderType provider;

    @Column(name = "provider_user_id", nullable = false, length = 255)
    private String providerUserId;

    @Column(name = "email", length = 254)
    private String email;
}
