package com.careeros.career.entity;

import com.careeros.common.entity.AuditableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "companies")
@Getter
@Setter
@SQLDelete(sql = "UPDATE companies SET deleted = true, deleted_at = NOW() WHERE id = ? AND version = ?")
@Where(clause = "deleted = false")
public class Company extends AuditableEntity {

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "industry", length = 100)
    private String industry;

    @Enumerated(EnumType.STRING)
    @Column(name = "hiring_status", length = 50)
    private HiringStatus hiringStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "interview_difficulty", length = 50)
    private InterviewDifficulty interviewDifficulty;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "tech_stack", columnDefinition = "jsonb")
    private List<String> techStack;

    @Column(name = "placement_category", length = 100)
    private String placementCategory;

    @Column(name = "eligibility_criteria", columnDefinition = "TEXT")
    private String eligibilityCriteria;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "hiring_seasons", columnDefinition = "jsonb")
    private List<String> hiringSeasons;

    @Column(name = "website_url")
    private String websiteUrl;

    @Column(name = "logo_url")
    private String logoUrl;

    @Column(name = "deleted", nullable = false)
    private Boolean deleted = false;

    @Column(name = "deleted_at")
    private Instant deletedAt;
}
