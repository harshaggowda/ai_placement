package com.careeros.career.entity;

import com.careeros.common.entity.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "certificates")
@Getter
@Setter
@SQLDelete(sql = "UPDATE certificates SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
public class Certificate extends AuditableEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "issuer", nullable = false, length = 255)
    private String issuer;

    @Column(name = "issue_date")
    private LocalDate issueDate;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "url", length = 1000)
    private String url;

    @Column(name = "credential_id", length = 255)
    private String credentialId;

    @Column(name = "deleted", nullable = false)
    private boolean deleted = false;
}
