package com.careeros.career.repository;

import com.careeros.career.entity.Resume;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ResumeRepository extends JpaRepository<Resume, UUID> {
    
    List<Resume> findAllByUserId(UUID userId);
    
    Optional<Resume> findByIdAndUserId(UUID id, UUID userId);
    
    // Custom query to find the latest version could be useful
    Optional<Resume> findFirstByUserIdOrderByVersionNumberDesc(UUID userId);
}
