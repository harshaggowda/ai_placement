package com.careeros.auth.repository;

import com.careeros.auth.entity.User;
import com.careeros.auth.entity.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Data access for {@link User}. Soft-deleted rows are excluded automatically (see entity filter).
 * Contracts only — no business logic in this phase.
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<User> findByEmailAndStatus(String email, UserStatus status);

    Page<User> findByStatus(UserStatus status, Pageable pageable);
}
