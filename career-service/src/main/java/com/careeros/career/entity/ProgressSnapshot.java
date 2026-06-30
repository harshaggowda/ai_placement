package com.careeros.career.entity;

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
@Table(name = "progress_snapshots")
@Getter
@Setter
@SQLDelete(sql = "UPDATE progress_snapshots SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
public class ProgressSnapshot extends AuditableEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "snapshot_date", nullable = false)
    private LocalDate snapshotDate;

    @Column(name = "applications_sent", nullable = false)
    private int applicationsSent = 0;

    @Column(name = "interviews_scheduled", nullable = false)
    private int interviewsScheduled = 0;

    @Column(name = "offers_received", nullable = false)
    private int offersReceived = 0;

    @Column(name = "rejections", nullable = false)
    private int rejections = 0;

    @Column(name = "active_projects", nullable = false)
    private int activeProjects = 0;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "active_skills", columnDefinition = "jsonb")
    private List<String> activeSkills = new ArrayList<>();

    @Column(name = "deleted", nullable = false)
    private boolean deleted = false;
}
