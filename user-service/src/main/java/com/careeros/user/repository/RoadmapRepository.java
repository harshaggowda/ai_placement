package com.careeros.user.repository;

import com.careeros.user.entity.Roadmap;
import com.careeros.user.entity.RoadmapStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/** Data access for {@link Roadmap}. Contracts only. */
@Repository
public interface RoadmapRepository extends JpaRepository<Roadmap, UUID> {

    Page<Roadmap> findAllByUserId(UUID userId, Pageable pageable);

    Optional<Roadmap> findByIdAndUserId(UUID id, UUID userId);

    long countByUserIdAndStatus(UUID userId, RoadmapStatus status);

    Page<Roadmap> findAllByUserIdAndStatus(UUID userId, RoadmapStatus status, Pageable pageable);

    Page<Roadmap> findAllByGoalId(UUID goalId, Pageable pageable);

    Page<Roadmap> findAllByIsTemplateTrue(Pageable pageable);
}
