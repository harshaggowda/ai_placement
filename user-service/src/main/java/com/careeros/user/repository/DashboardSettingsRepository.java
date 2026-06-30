package com.careeros.user.repository;

import com.careeros.user.entity.DashboardSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/** Data access for {@link DashboardSettings}. Contracts only. */
@Repository
public interface DashboardSettingsRepository extends JpaRepository<DashboardSettings, UUID> {

    Optional<DashboardSettings> findByUserId(UUID userId);

    boolean existsByUserId(UUID userId);
}
