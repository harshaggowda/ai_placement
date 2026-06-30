package com.careeros.user.repository;

import com.careeros.user.entity.Goal;
import com.careeros.user.entity.GoalCategory;
import com.careeros.user.entity.GoalPriority;
import com.careeros.user.entity.GoalStatus;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

/**
 * Type-safe, composable JPA specifications for filtered goal queries. {@code ownedBy} is always
 * applied first so searches can never cross user boundaries.
 */
public final class GoalSpecifications {

    private GoalSpecifications() {
    }

    public static Specification<Goal> ownedBy(UUID userId) {
        return (root, query, cb) -> cb.equal(root.get("userId"), userId);
    }

    public static Specification<Goal> hasStatus(GoalStatus status) {
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<Goal> hasCategory(GoalCategory category) {
        return (root, query, cb) -> cb.equal(root.get("category"), category);
    }

    public static Specification<Goal> hasPriority(GoalPriority priority) {
        return (root, query, cb) -> cb.equal(root.get("priority"), priority);
    }
}
