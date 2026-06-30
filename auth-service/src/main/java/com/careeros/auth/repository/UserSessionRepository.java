package com.careeros.auth.repository;

import com.careeros.auth.entity.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Data access for {@link UserSession}. Contracts only.
 */
@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, UUID> {

    List<UserSession> findAllByUserIdAndRevokedAtIsNull(UUID userId);

    long deleteByExpiresAtBefore(Instant cutoff);
}
