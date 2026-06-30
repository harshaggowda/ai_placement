package com.careeros.user.repository;

import com.careeros.user.entity.Skill;
import com.careeros.user.entity.SkillCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Data access for the {@link Skill} catalog. Contracts only. */
@Repository
public interface SkillRepository extends JpaRepository<Skill, UUID> {

    Optional<Skill> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);

    Page<Skill> findByCategory(SkillCategory category, Pageable pageable);

    Page<Skill> findByNameContainingIgnoreCase(String keyword, Pageable pageable);

    Page<Skill> findByCategoryAndNameContainingIgnoreCase(SkillCategory category, String keyword, Pageable pageable);

    /** Single-query replacement for the per-category N+1 pattern. Returns {@code [category, count]} pairs. */
    @Query("SELECT s.category, COUNT(s) FROM Skill s GROUP BY s.category")
    List<Object[]> countGroupedByCategory();

    long countByCategory(SkillCategory category);
}
