package com.careeros.career.repository;

import com.careeros.career.entity.ProgressSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProgressSnapshotRepository extends JpaRepository<ProgressSnapshot, UUID> {
    List<ProgressSnapshot> findAllByUserIdOrderBySnapshotDateDesc(UUID userId);
    Optional<ProgressSnapshot> findByIdAndUserId(UUID id, UUID userId);
    Optional<ProgressSnapshot> findBySnapshotDateAndUserId(LocalDate snapshotDate, UUID userId);
}
