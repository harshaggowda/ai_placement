package com.careeros.user.repository;

import com.careeros.user.entity.NotificationPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/** Data access for {@link NotificationPreference}. Contracts only. */
@Repository
public interface NotificationPreferenceRepository extends JpaRepository<NotificationPreference, UUID> {

    Optional<NotificationPreference> findByUserId(UUID userId);

    boolean existsByUserId(UUID userId);
}
