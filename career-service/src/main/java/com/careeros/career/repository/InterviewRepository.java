package com.careeros.career.repository;

import com.careeros.career.entity.Interview;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InterviewRepository extends JpaRepository<Interview, UUID> {

    @EntityGraph(attributePaths = {"application", "application.company"})
    List<Interview> findAllByUserId(UUID userId);

    @EntityGraph(attributePaths = {"application", "application.company"})
    List<Interview> findAllByApplicationIdAndUserId(UUID applicationId, UUID userId);

    @EntityGraph(attributePaths = {"application", "application.company"})
    Optional<Interview> findByIdAndUserId(UUID id, UUID userId);
}
