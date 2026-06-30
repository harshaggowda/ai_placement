package com.careeros.career.entity;

import com.careeros.career.dto.PlannerTask;
import com.careeros.common.entity.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "daily_planners")
@Getter
@Setter
@SQLDelete(sql = "UPDATE daily_planners SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
public class DailyPlanner extends AuditableEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "target_date", nullable = false)
    private LocalDate targetDate;

    @Column(name = "focus_area", length = 255)
    private String focusArea;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "tasks", columnDefinition = "jsonb")
    private List<PlannerTask> tasks = new ArrayList<>();

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "completed", nullable = false)
    private boolean completed = false;

    @Column(name = "deleted", nullable = false)
    private boolean deleted = false;
}
