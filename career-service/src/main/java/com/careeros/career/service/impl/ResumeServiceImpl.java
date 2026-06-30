package com.careeros.career.service.impl;

import com.careeros.career.dto.ResumeAnalysisMetadataDto;
import com.careeros.career.dto.ResumeAnalysisMetadataResponseDto;
import com.careeros.career.dto.ResumeDto;
import com.careeros.career.dto.ResumeResponseDto;
import com.careeros.career.entity.Resume;
import com.careeros.career.entity.ResumeAnalysisMetadata;
import com.careeros.career.entity.ResumeStatus;
import com.careeros.career.event.ResumeUploadedEvent;
import com.careeros.career.event.ResumeVersionCreatedEvent;
import com.careeros.career.mapper.ResumeAnalysisMetadataMapper;
import com.careeros.career.mapper.ResumeMapper;
import com.careeros.career.repository.ResumeAnalysisMetadataRepository;
import com.careeros.career.repository.ResumeRepository;
import com.careeros.career.service.ResumeService;
import com.careeros.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResumeServiceImpl implements ResumeService {

    private final ResumeRepository resumeRepository;
    private final ResumeAnalysisMetadataRepository metadataRepository;
    private final ResumeMapper resumeMapper;
    private final ResumeAnalysisMetadataMapper metadataMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional(readOnly = true)
    public List<ResumeResponseDto> getAllResumes(UUID userId) {
        return resumeRepository.findAllByUserId(userId).stream()
                .map(resumeMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ResumeResponseDto getResume(UUID userId, UUID resumeId) {
        return resumeMapper.toResponse(getResumeEntity(userId, resumeId));
    }

    @Override
    @Transactional
    public ResumeResponseDto createResume(UUID userId, ResumeDto dto) {
        Resume resume = resumeMapper.toEntity(dto);
        resume.setUserId(userId);
        resume.setVersionNumber(1);

        Resume saved = resumeRepository.save(resume);
        
        eventPublisher.publishEvent(new ResumeUploadedEvent(saved.getId(), userId, saved.getFileUrl()));
        
        return resumeMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public ResumeResponseDto updateResume(UUID userId, UUID resumeId, ResumeDto dto) {
        Resume resume = getResumeEntity(userId, resumeId);
        resumeMapper.updateEntity(dto, resume);
        return resumeMapper.toResponse(resume);
    }

    @Override
    @Transactional
    public ResumeResponseDto createNewVersion(UUID userId, UUID resumeId, ResumeDto dto) {
        Resume oldResume = getResumeEntity(userId, resumeId);
        oldResume.setStatus(ResumeStatus.ARCHIVED); // Archive old version

        Resume newResume = resumeMapper.toEntity(dto);
        newResume.setUserId(userId);
        newResume.setVersionNumber(oldResume.getVersionNumber() + 1);

        Resume saved = resumeRepository.save(newResume);

        eventPublisher.publishEvent(new ResumeVersionCreatedEvent(saved.getId(), userId, saved.getVersionNumber(), saved.getFileUrl()));

        return resumeMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void deleteResume(UUID userId, UUID resumeId) {
        Resume resume = getResumeEntity(userId, resumeId);
        resumeRepository.delete(resume); // Soft delete handled by @SQLDelete
    }

    @Override
    @Transactional(readOnly = true)
    public ResumeAnalysisMetadataResponseDto getAnalysisMetadata(UUID userId, UUID resumeId) {
        // Ensure user owns the resume
        getResumeEntity(userId, resumeId);
        
        return metadataRepository.findAllByResumeId(resumeId).stream()
                .findFirst() // Simplification: get latest/first metadata for this resume ID
                .map(metadataMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Resume Analysis Metadata not found"));
    }

    @Override
    @Transactional
    public ResumeAnalysisMetadataResponseDto saveAnalysisMetadata(UUID userId, UUID resumeId, ResumeAnalysisMetadataDto dto) {
        Resume resume = getResumeEntity(userId, resumeId);

        ResumeAnalysisMetadata metadata = metadataRepository.findAllByResumeId(resumeId).stream().findFirst()
                .orElseGet(() -> {
                    ResumeAnalysisMetadata newMetadata = new ResumeAnalysisMetadata();
                    newMetadata.setResumeId(resumeId);
                    return newMetadata;
                });

        metadataMapper.updateEntity(dto, metadata);
        metadata.setResumeVersion(resume.getVersionNumber());

        ResumeAnalysisMetadata saved = metadataRepository.save(metadata);
        return metadataMapper.toResponse(saved);
    }

    private Resume getResumeEntity(UUID userId, UUID resumeId) {
        return resumeRepository.findByIdAndUserId(resumeId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found or access denied"));
    }
}
