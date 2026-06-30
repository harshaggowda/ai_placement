package com.careeros.auth.entity;

import com.careeros.common.entity.SoftDeletableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.util.HashSet;
import java.util.Set;

/**
 * A named role grouping a set of {@link Permission}s (RBAC).
 *
 * <p>Owning side of the role↔permission many-to-many. Role names are unique and conventionally
 * {@code ROLE_}-prefixed (e.g. {@code ROLE_USER}, {@code ROLE_ADMIN}). Soft-deleted reference data.
 */
@Getter
@Setter
@Entity
@Table(
        name = "roles",
        uniqueConstraints = @UniqueConstraint(name = "uk_roles_name", columnNames = "name"))
@SQLDelete(sql = "UPDATE roles SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class Role extends SoftDeletableEntity {

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "description", length = 255)
    private String description;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "role_permissions",
            joinColumns = @JoinColumn(name = "role_id",
                    foreignKey = @ForeignKey(name = "fk_role_permissions_role")),
            inverseJoinColumns = @JoinColumn(name = "permission_id",
                    foreignKey = @ForeignKey(name = "fk_role_permissions_permission")),
            uniqueConstraints = @UniqueConstraint(name = "uk_role_permissions",
                    columnNames = {"role_id", "permission_id"}))
    private Set<Permission> permissions = new HashSet<>();
}
