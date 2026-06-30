package com.careeros.auth.repository;

import com.careeros.auth.entity.OAuthProvider;
import com.careeros.auth.entity.OAuthProviderType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Data access for {@link OAuthProvider} links. Contracts only.
 */
@Repository
public interface OAuthProviderRepository extends JpaRepository<OAuthProvider, UUID> {

    Optional<OAuthProvider> findByProviderAndProviderUserId(OAuthProviderType provider, String providerUserId);

    List<OAuthProvider> findAllByUserId(UUID userId);

    boolean existsByUserIdAndProvider(UUID userId, OAuthProviderType provider);
}
