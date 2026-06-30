package com.careeros.user.repository;

import com.careeros.user.entity.Achievement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/** Data access for the {@link Achievement} catalog. Contracts only. */
@Repository
public interface AchievementRepository extends JpaRepository<Achievement, UUID> {

    Optional<Achievement> findByCode(String code);

    boolean existsByCode(String code);
}
