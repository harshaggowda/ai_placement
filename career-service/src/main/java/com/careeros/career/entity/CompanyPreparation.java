package com.careeros.career.entity;

import com.careeros.common.entity.AuditableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "company_preparations")
@Getter
@Setter
public class CompanyPreparation extends AuditableEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Enumerated(EnumType.STRING)
    @Column(name = "preparation_status", nullable = false, length = 50)
    private PreparationStatus preparationStatus = PreparationStatus.NOT_STARTED;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", length = 50)
    private Priority priority;

    @Column(name = "completion_percentage")
    private Integer completionPercentage = 0;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "study_progress", columnDefinition = "jsonb")
    private Object studyProgress; // Storing as unstructured JSON or custom mapped object

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "bookmarks", columnDefinition = "jsonb")
    private List<UUID> bookmarks;
}
