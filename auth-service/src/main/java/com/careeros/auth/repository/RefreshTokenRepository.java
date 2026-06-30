package com.careeros.auth.repository;

import com.careeros.auth.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Data access for {@link RefreshToken}. Contracts only — includes lookup, listing, and expiry/
 * revocation cleanup hooks.
 */
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    List<RefreshToken> findAllByUserId(UUID userId);

    long deleteByExpiresAtBefore(Instant cutoff);

    long deleteByUserId(UUID userId);
}
