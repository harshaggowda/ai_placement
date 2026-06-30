package com.careeros.user.repository;

import com.careeros.user.entity.SkillProficiency;
import com.careeros.user.entity.UserSkill;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Data access for {@link UserSkill}. Uses {@code @EntityGraph} to JOIN FETCH the {@code Skill}
 * catalog entry in a single query, preventing N+1 reads on listing.
 */
@Repository
public interface UserSkillRepository extends JpaRepository<UserSkill, UUID> {

    @EntityGraph(attributePaths = "skill")
    List<UserSkill> findAllByUserId(UUID userId);

    @EntityGraph(attributePaths = "skill")
    Optional<UserSkill> findByUserIdAndSkillId(UUID userId, UUID skillId);

    boolean existsByUserIdAndSkillId(UUID userId, UUID skillId);

    long countByUserId(UUID userId);

    long countByUserIdAndProficiency(UUID userId, SkillProficiency proficiency);

    @Query("SELECT us.proficiency, COUNT(us) FROM UserSkill us WHERE us.userId = :userId GROUP BY us.proficiency")
    List<Object[]> countByProficiencyForUser(@Param("userId") UUID userId);
}
