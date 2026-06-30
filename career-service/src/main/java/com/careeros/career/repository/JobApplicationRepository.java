package com.careeros.career.repository;

import com.careeros.career.entity.JobApplication;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JobApplicationRepository extends JpaRepository<JobApplication, UUID> {

    @EntityGraph(attributePaths = {"company"})
    List<JobApplication> findAllByUserId(UUID userId);

    @EntityGraph(attributePaths = {"company"})
    Optional<JobApplication> findByIdAndUserId(UUID id, UUID userId);
}
