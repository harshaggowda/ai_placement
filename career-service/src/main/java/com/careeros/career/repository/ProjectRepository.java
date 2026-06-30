package com.careeros.career.repository;

import com.careeros.career.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProjectRepository extends JpaRepository<Project, UUID> {
    List<Project> findAllByUserId(UUID userId);
    Optional<Project> findByIdAndUserId(UUID id, UUID userId);
}
