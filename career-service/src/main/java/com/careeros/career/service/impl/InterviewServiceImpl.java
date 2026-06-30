package com.careeros.career.service.impl;

import com.careeros.career.dto.InterviewDto;
import com.careeros.career.dto.InterviewResponseDto;
import com.careeros.career.entity.Interview;
import com.careeros.career.entity.JobApplication;
import com.careeros.career.mapper.InterviewMapper;
import com.careeros.career.repository.InterviewRepository;
import com.careeros.career.repository.JobApplicationRepository;
import com.careeros.career.service.InterviewService;
import com.careeros.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class InterviewServiceImpl implements InterviewService {

    private final InterviewRepository interviewRepository;
    private final JobApplicationRepository applicationRepository;
    private final InterviewMapper interviewMapper;

    @Override
    @Transactional(readOnly = true)
    public List<InterviewResponseDto> getAllInterviews(UUID userId) {
        return interviewRepository.findAllByUserId(userId).stream()
                .map(interviewMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<InterviewResponseDto> getInterviewsForApplication(UUID userId, UUID applicationId) {
        return interviewRepository.findAllByApplicationIdAndUserId(applicationId, userId).stream()
                .map(interviewMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public InterviewResponseDto getInterview(UUID userId, UUID interviewId) {
        Interview interview = getInterviewEntity(userId, interviewId);
        return interviewMapper.toResponse(interview);
    }

    @Override
    @Transactional
    public InterviewResponseDto createInterview(UUID userId, InterviewDto dto) {
        JobApplication application = applicationRepository.findByIdAndUserId(dto.applicationId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found or access denied"));

        Interview interview = interviewMapper.toEntity(dto);
        interview.setUserId(userId);
        interview.setApplication(application);

        Interview saved = interviewRepository.save(interview);
        return interviewMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public InterviewResponseDto updateInterview(UUID userId, UUID interviewId, InterviewDto dto) {
        Interview interview = getInterviewEntity(userId, interviewId);

        if (!interview.getApplication().getId().equals(dto.applicationId())) {
            JobApplication newApplication = applicationRepository.findByIdAndUserId(dto.applicationId(), userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Target application not found"));
            interview.setApplication(newApplication);
        }

        interviewMapper.updateEntity(dto, interview);

        Interview updated = interviewRepository.save(interview);
        return interviewMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void deleteInterview(UUID userId, UUID interviewId) {
        Interview interview = getInterviewEntity(userId, interviewId);
        interviewRepository.delete(interview);
    }

    private Interview getInterviewEntity(UUID userId, UUID interviewId) {
        return interviewRepository.findByIdAndUserId(interviewId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Interview not found or access denied"));
    }
}
