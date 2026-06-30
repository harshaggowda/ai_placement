package com.careeros.career.service;

import com.careeros.career.dto.ResumeAnalysisMetadataDto;
import com.careeros.career.dto.ResumeAnalysisMetadataResponseDto;
import com.careeros.career.dto.ResumeDto;
import com.careeros.career.dto.ResumeResponseDto;
import com.careeros.career.entity.ResumeStatus;

import java.util.List;
import java.util.UUID;

public interface ResumeService {

    List<ResumeResponseDto> getAllResumes(UUID userId);

    ResumeResponseDto getResume(UUID userId, UUID resumeId);

    ResumeResponseDto createResume(UUID userId, ResumeDto dto);

    ResumeResponseDto updateResume(UUID userId, UUID resumeId, ResumeDto dto);
    
    ResumeResponseDto createNewVersion(UUID userId, UUID resumeId, ResumeDto dto);

    void deleteResume(UUID userId, UUID resumeId);

    // Analysis Metadata
    ResumeAnalysisMetadataResponseDto getAnalysisMetadata(UUID userId, UUID resumeId);

    ResumeAnalysisMetadataResponseDto saveAnalysisMetadata(UUID userId, UUID resumeId, ResumeAnalysisMetadataDto dto);
}
