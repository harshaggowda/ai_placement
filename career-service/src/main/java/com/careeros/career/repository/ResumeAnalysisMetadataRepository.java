package com.careeros.career.repository;

import com.careeros.career.entity.ResumeAnalysisMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ResumeAnalysisMetadataRepository extends JpaRepository<ResumeAnalysisMetadata, UUID> {

    List<ResumeAnalysisMetadata> findAllByResumeId(UUID resumeId);
    
    Optional<ResumeAnalysisMetadata> findByAnalysisId(String analysisId);
}
