package com.careeros.career.service;

import com.careeros.career.dto.JobApplicationDto;
import com.careeros.career.dto.JobApplicationResponseDto;

import java.util.List;
import java.util.UUID;

public interface JobApplicationService {

    List<JobApplicationResponseDto> getAllApplications(UUID userId);

    JobApplicationResponseDto getApplication(UUID userId, UUID applicationId);

    JobApplicationResponseDto createApplication(UUID userId, JobApplicationDto dto);

    JobApplicationResponseDto updateApplication(UUID userId, UUID applicationId, JobApplicationDto dto);

    void deleteApplication(UUID userId, UUID applicationId);
}
