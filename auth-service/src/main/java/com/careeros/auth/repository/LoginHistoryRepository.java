package com.careeros.auth.repository;

import com.careeros.auth.entity.LoginHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Data access for the append-only {@link LoginHistory}. Contracts only.
 */
@Repository
public interface LoginHistoryRepository extends JpaRepository<LoginHistory, UUID> {

    Page<LoginHistory> findAllByUserId(UUID userId, Pageable pageable);

    Page<LoginHistory> findAllByEmail(String email, Pageable pageable);
}
