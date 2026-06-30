package com.careeros.user.repository;

import com.careeros.user.entity.UserAchievement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Data access for {@link UserAchievement}. Uses {@link EntityGraph} to JOIN FETCH the achievement
 * catalog entry when listing, preventing N+1.
 */
@Repository
public interface UserAchievementRepository extends JpaRepository<UserAchievement, UUID> {

    @EntityGraph(attributePaths = "achievement")
    Page<UserAchievement> findAllByUserId(UUID userId, Pageable pageable);

    Optional<UserAchievement> findByUserIdAndAchievementCode(UUID userId, String code);

    boolean existsByUserIdAndAchievementCode(UUID userId, String code);

    long countByUserId(UUID userId);
}
