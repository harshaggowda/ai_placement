package com.careeros.user.entity;

import com.careeros.common.entity.SoftDeletableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * An ordered, atomic task within a {@link Roadmap}.
 *
 * <p>{@code order_index} drives display ordering; the combination {@code (roadmap_id, order_index)}
 * must be unique (enforced by the application, not a DB constraint, since gaps are allowed).
 * {@code dependency_task_id} is a nullable self-reference stored as a UUID (not a JPA relationship)
 * for simple, non-circular task sequencing.
 */
@Getter
@Setter
@Entity
@Table(
        name = "roadmap_tasks",
        indexes = {
                @Index(name = "idx_roadmap_tasks_roadmap", columnList = "roadmap_id"),
                @Index(name = "idx_roadmap_tasks_status", columnList = "status")
        })
@SQLDelete(sql = "UPDATE roadmap_tasks SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class RoadmapTask extends SoftDeletableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "roadmap_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_roadmap_tasks_roadmap"))
    private Roadmap roadmap;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TaskStatus status = TaskStatus.TODO;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 10)
    private TaskPriority priority = TaskPriority.MEDIUM;

    @Column(name = "order_index", nullable = false)
    private int orderIndex;

    @Column(name = "estimated_hours", precision = 6, scale = 2)
    private BigDecimal estimatedHours;

    @Column(name = "actual_hours", precision = 6, scale = 2)
    private BigDecimal actualHours;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "completed_at")
    private Instant completedAt;

    /** Optional dependency: this task should not start before the referenced task is complete. */
    @Column(name = "dependency_task_id")
    private UUID dependencyTaskId;
}
