package com.careeros.career.service.impl;

import com.careeros.career.dto.ResumeDto;
import com.careeros.career.dto.ResumeResponseDto;
import com.careeros.career.entity.Resume;
import com.careeros.career.entity.ResumeStatus;
import com.careeros.career.event.ResumeUploadedEvent;
import com.careeros.career.event.ResumeVersionCreatedEvent;
import com.careeros.career.mapper.ResumeAnalysisMetadataMapper;
import com.careeros.career.mapper.ResumeMapper;
import com.careeros.career.repository.ResumeAnalysisMetadataRepository;
import com.careeros.career.repository.ResumeRepository;
import com.careeros.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResumeServiceImplTest {

    @Mock ResumeRepository resumeRepository;
    @Mock ResumeAnalysisMetadataRepository metadataRepository;
    @Mock ResumeMapper resumeMapper;
    @Mock ResumeAnalysisMetadataMapper metadataMapper;
    @Mock ApplicationEventPublisher eventPublisher;

    @InjectMocks ResumeServiceImpl resumeService;

    private final UUID userId = UUID.randomUUID();
    private final UUID resumeId = UUID.randomUUID();

    @Test
    void createResumeSavesEntityAndPublishesEvent() {
        ResumeDto dto = new ResumeDto("My Resume", "http://s3/file.pdf", ResumeStatus.DRAFT, List.of("Java"), false);
        Resume entity = new Resume();
        entity.setId(resumeId);
        entity.setFileUrl("http://s3/file.pdf");
        
        when(resumeMapper.toEntity(dto)).thenReturn(entity);
        when(resumeRepository.save(entity)).thenReturn(entity);
        when(resumeMapper.toResponse(entity)).thenReturn(mockResponse());

        ResumeResponseDto response = resumeService.createResume(userId, dto);

        verify(resumeRepository).save(entity);
        verify(eventPublisher).publishEvent(any(ResumeUploadedEvent.class));
        assertThat(response).isNotNull();
    }

    @Test
    void createNewVersionArchivesOldAndPublishesEvent() {
        ResumeDto dto = new ResumeDto("New Version", "http://s3/file-v2.pdf", ResumeStatus.PUBLISHED, List.of("Java"), false);
        Resume oldResume = new Resume();
        oldResume.setId(resumeId);
        oldResume.setVersionNumber(1);

        Resume newResume = new Resume();
        newResume.setId(UUID.randomUUID());
        newResume.setFileUrl("http://s3/file-v2.pdf");
        newResume.setVersionNumber(2);

        when(resumeRepository.findByIdAndUserId(resumeId, userId)).thenReturn(Optional.of(oldResume));
        when(resumeMapper.toEntity(dto)).thenReturn(newResume);
        when(resumeRepository.save(newResume)).thenReturn(newResume);
        when(resumeMapper.toResponse(newResume)).thenReturn(mockResponse());

        resumeService.createNewVersion(userId, resumeId, dto);

        assertThat(oldResume.getStatus()).isEqualTo(ResumeStatus.ARCHIVED);
        verify(resumeRepository).save(newResume);
        verify(eventPublisher).publishEvent(any(ResumeVersionCreatedEvent.class));
    }

    @Test
    void getResumeThrowsNotFoundWhenNotOwned() {
        when(resumeRepository.findByIdAndUserId(resumeId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> resumeService.getResume(userId, resumeId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    private ResumeResponseDto mockResponse() {
        return new ResumeResponseDto(resumeId, userId, "Title", "URL", ResumeStatus.DRAFT, 1, List.of(), false, null, null);
    }
}
