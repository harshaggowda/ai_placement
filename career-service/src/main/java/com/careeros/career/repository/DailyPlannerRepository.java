package com.careeros.career.repository;

import com.careeros.career.entity.DailyPlanner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DailyPlannerRepository extends JpaRepository<DailyPlanner, UUID> {
    List<DailyPlanner> findAllByUserIdOrderByTargetDateDesc(UUID userId);
    Optional<DailyPlanner> findByIdAndUserId(UUID id, UUID userId);
    Optional<DailyPlanner> findByTargetDateAndUserId(LocalDate targetDate, UUID userId);
}
