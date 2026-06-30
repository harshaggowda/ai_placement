package com.careeros.user.repository;

import com.careeros.user.entity.RoadmapTask;
import com.careeros.user.entity.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Data access for {@link RoadmapTask}. Contracts only. */
@Repository
public interface RoadmapTaskRepository extends JpaRepository<RoadmapTask, UUID> {

    /** Ordered task list for a roadmap — prevents N+1 via direct roadmap association. */
    List<RoadmapTask> findAllByRoadmapIdOrderByOrderIndex(UUID roadmapId);

    Optional<RoadmapTask> findByIdAndRoadmapId(UUID id, UUID roadmapId);

    long countByRoadmapId(UUID roadmapId);

    long countByRoadmapIdAndStatus(UUID roadmapId, TaskStatus status);

    /** Due today across all roadmaps for a user — used for the dashboard. */
    @Query("""
            SELECT t FROM RoadmapTask t
            WHERE t.roadmap.userId = :userId
              AND t.dueDate = :today
              AND t.status <> com.careeros.user.entity.TaskStatus.COMPLETED
              AND t.status <> com.careeros.user.entity.TaskStatus.SKIPPED
            ORDER BY t.priority DESC, t.orderIndex ASC
            """)
    List<RoadmapTask> findTodayTasksForUser(@Param("userId") UUID userId, @Param("today") LocalDate today);
}
