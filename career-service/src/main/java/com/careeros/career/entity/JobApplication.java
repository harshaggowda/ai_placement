package com.careeros.career.entity;

import com.careeros.common.entity.AuditableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "job_applications")
@Getter
@Setter
@SQLDelete(sql = "UPDATE job_applications SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
public class JobApplication extends AuditableEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(name = "resume_id")
    private UUID resumeId;

    @Column(name = "role", length = 255)
    private String role;

    @Column(name = "url", length = 1000)
    private String url;

    @Column(name = "referer", length = 255)
    private String referer;

    @Column(name = "location", length = 255)
    private String location;

    @Column(name = "offer_details", columnDefinition = "TEXT")
    private String offerDetails;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private ApplicationStatus status = ApplicationStatus.SAVED;

    @Column(name = "applied_at")
    private Instant appliedAt;

    @Column(name = "deleted", nullable = false)
    private boolean deleted = false;
}
