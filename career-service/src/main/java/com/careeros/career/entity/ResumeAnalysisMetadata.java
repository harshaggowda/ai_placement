package com.careeros.career.entity;

import com.careeros.common.entity.AuditableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "resume_analysis_metadata")
@Getter
@Setter
public class ResumeAnalysisMetadata extends AuditableEntity {

    @Column(name = "resume_id", nullable = false)
    private UUID resumeId;

    @Column(name = "analysis_id", length = 100)
    private String analysisId;

    @Column(name = "resume_version", nullable = false)
    private Integer resumeVersion;

    @Enumerated(EnumType.STRING)
    @Column(name = "analysis_status", nullable = false, length = 50)
    private AnalysisStatus analysisStatus;

    @Column(name = "ai_provider", length = 100)
    private String aiProvider;

    @Column(name = "generated_time")
    private Instant generatedTime;

    @Column(name = "processing_time_ms")
    private Long processingTimeMs;
}
