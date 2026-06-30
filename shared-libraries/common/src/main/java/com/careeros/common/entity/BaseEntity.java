package com.careeros.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * Root persistence superclass for every aggregate root / entity in the platform.
 *
 * <p>Provides a database-agnostic {@link UUID} identity and an optimistic-locking {@code version}.
 * Identity-based {@link #equals(Object)} / {@link #hashCode()} follow the Vladimir Khorikov /
 * Hibernate-safe pattern so that proxy instances and detached entities compare correctly.
 *
 * <p>Audit columns are deliberately separated into {@link AuditableEntity} so that non-audited
 * reference data can extend this class directly.
 */
@Getter
@Setter
@MappedSuperclass
public abstract class BaseEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    /**
     * Entities are equal when they share a persisted identity and a compatible (proxy-aware) type.
     * Transient entities (null id) are only equal to themselves.
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null) {
            return false;
        }
        if (!(other instanceof BaseEntity that)) {
            return false;
        }
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        // Constant hashCode keeps entities usable in hash collections before and after persistence.
        return Objects.hash(getClass().hashCode());
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{id=" + id + ", version=" + version + "}";
    }
}
