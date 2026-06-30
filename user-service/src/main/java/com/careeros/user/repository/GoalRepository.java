package com.careeros.user.repository;

import com.careeros.user.entity.Goal;
import com.careeros.user.entity.GoalStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Data access for {@link Goal}. Extends {@link JpaSpecificationExecutor} for dynamic, type-safe
 * filtered searches (status/category/priority + sorting + pagination). Contracts only.
 */
@Repository
public interface GoalRepository extends JpaRepository<Goal, UUID>, JpaSpecificationExecutor<Goal> {

    Page<Goal> findAllByUserId(UUID userId, Pageable pageable);

    /** Ownership-safe lookup: returns the goal only if it belongs to the given user. */
    Optional<Goal> findByIdAndUserId(UUID id, UUID userId);

    long countByUserIdAndStatus(UUID userId, GoalStatus status);
}
