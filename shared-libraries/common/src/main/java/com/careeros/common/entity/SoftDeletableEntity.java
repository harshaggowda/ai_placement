package com.careeros.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * Superclass for entities that are <strong>soft-deleted</strong> rather than physically removed.
 *
 * <p>Adds a nullable {@code deleted_at} marker on top of the audit metadata in
 * {@link AuditableEntity}. Concrete entities opt in to soft delete by extending this class and
 * declaring the Hibernate filters:
 *
 * <pre>{@code
 * @SQLDelete(sql = "UPDATE <table> SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
 * @SQLRestriction("deleted_at IS NULL")
 * }</pre>
 *
 * <p>With those annotations a {@code delete} becomes an {@code UPDATE} and every query transparently
 * excludes deleted rows, preserving history and referential integrity. Reference/aggregate data
 * (users, roles, plans, resumes…) is soft-deleted; short-lived records (tokens, sessions, webhook
 * events) are hard-deleted or expired and extend {@link AuditableEntity} directly.
 */
@Getter
@Setter
@MappedSuperclass
public abstract class SoftDeletableEntity extends AuditableEntity {

    @Column(name = "deleted_at")
    private Instant deletedAt;

    /** Whether this row has been soft-deleted. */
    public boolean isDeleted() {
        return deletedAt != null;
    }
}
