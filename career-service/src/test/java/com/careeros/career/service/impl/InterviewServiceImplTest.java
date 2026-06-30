package com.careeros.career.service.impl;

import com.careeros.career.dto.InterviewDto;
import com.careeros.career.dto.InterviewResponseDto;
import com.careeros.career.entity.Interview;
import com.careeros.career.entity.InterviewFormat;
import com.careeros.career.entity.InterviewStatus;
import com.careeros.career.entity.InterviewType;
import com.careeros.career.entity.JobApplication;
import com.careeros.career.mapper.InterviewMapper;
import com.careeros.career.repository.InterviewRepository;
import com.careeros.career.repository.JobApplicationRepository;
import com.careeros.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InterviewServiceImplTest {

    @Mock InterviewRepository interviewRepository;
    @Mock JobApplicationRepository applicationRepository;
    @Mock InterviewMapper interviewMapper;

    @InjectMocks InterviewServiceImpl interviewService;

    private final UUID userId = UUID.randomUUID();
    private final UUID applicationId = UUID.randomUUID();
    private final UUID interviewId = UUID.randomUUID();

    @Test
    void createInterviewSucceedsWhenApplicationOwned() {
        InterviewDto dto = new InterviewDto(applicationId, InterviewType.TECHNICAL, InterviewFormat.VIRTUAL, InterviewStatus.SCHEDULED, null, "Round 1", null, null, null, null);
        
        JobApplication application = new JobApplication();
        application.setId(applicationId);
        application.setUserId(userId);
        
        Interview interview = new Interview();
        interview.setId(interviewId);

        when(applicationRepository.findByIdAndUserId(applicationId, userId)).thenReturn(Optional.of(application));
        when(interviewMapper.toEntity(dto)).thenReturn(interview);
        when(interviewRepository.save(interview)).thenReturn(interview);
        
        InterviewResponseDto responseMock = new InterviewResponseDto(
                interviewId, userId, applicationId, InterviewType.TECHNICAL, InterviewFormat.VIRTUAL, InterviewStatus.SCHEDULED, null, "Round 1", null, null, null, null, null, null);
        when(interviewMapper.toResponse(interview)).thenReturn(responseMock);

        InterviewResponseDto result = interviewService.createInterview(userId, dto);

        verify(interviewRepository).save(interview);
        assertThat(result.type()).isEqualTo(InterviewType.TECHNICAL);
    }

    @Test
    void createInterviewThrowsWhenApplicationNotOwned() {
        InterviewDto dto = new InterviewDto(applicationId, InterviewType.TECHNICAL, InterviewFormat.VIRTUAL, InterviewStatus.SCHEDULED, null, "Round 1", null, null, null, null);

        when(applicationRepository.findByIdAndUserId(applicationId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> interviewService.createInterview(userId, dto))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
