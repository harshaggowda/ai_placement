package com.careeros.career.service;

import com.careeros.career.dto.InterviewDto;
import com.careeros.career.dto.InterviewResponseDto;

import java.util.List;
import java.util.UUID;

public interface InterviewService {

    List<InterviewResponseDto> getAllInterviews(UUID userId);

    List<InterviewResponseDto> getInterviewsForApplication(UUID userId, UUID applicationId);

    InterviewResponseDto getInterview(UUID userId, UUID interviewId);

    InterviewResponseDto createInterview(UUID userId, InterviewDto dto);

    InterviewResponseDto updateInterview(UUID userId, UUID interviewId, InterviewDto dto);

    void deleteInterview(UUID userId, UUID interviewId);
}
