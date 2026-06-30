package com.careeros.career.service.impl;

import com.careeros.career.dto.JobApplicationDto;
import com.careeros.career.dto.JobApplicationResponseDto;
import com.careeros.career.entity.Company;
import com.careeros.career.entity.JobApplication;
import com.careeros.career.mapper.JobApplicationMapper;
import com.careeros.career.repository.CompanyRepository;
import com.careeros.career.repository.JobApplicationRepository;
import com.careeros.career.service.JobApplicationService;
import com.careeros.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobApplicationServiceImpl implements JobApplicationService {

    private final JobApplicationRepository applicationRepository;
    private final CompanyRepository companyRepository;
    private final JobApplicationMapper applicationMapper;

    @Override
    @Transactional(readOnly = true)
    public List<JobApplicationResponseDto> getAllApplications(UUID userId) {
        return applicationRepository.findAllByUserId(userId).stream()
                .map(applicationMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public JobApplicationResponseDto getApplication(UUID userId, UUID applicationId) {
        JobApplication application = getApplicationEntity(userId, applicationId);
        return applicationMapper.toResponse(application);
    }

    @Override
    @Transactional
    public JobApplicationResponseDto createApplication(UUID userId, JobApplicationDto dto) {
        Company company = companyRepository.findById(dto.companyId())
                .orElseThrow(() -> new ResourceNotFoundException("Company not found"));

        JobApplication application = applicationMapper.toEntity(dto);
        application.setUserId(userId);
        application.setCompany(company);
        application.setAppliedAt(Instant.now());

        JobApplication saved = applicationRepository.save(application);
        return applicationMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public JobApplicationResponseDto updateApplication(UUID userId, UUID applicationId, JobApplicationDto dto) {
        JobApplication application = getApplicationEntity(userId, applicationId);

        if (!application.getCompany().getId().equals(dto.companyId())) {
            Company company = companyRepository.findById(dto.companyId())
                    .orElseThrow(() -> new ResourceNotFoundException("Company not found"));
            application.setCompany(company);
        }

        applicationMapper.updateEntity(dto, application);
        
        JobApplication updated = applicationRepository.save(application);
        return applicationMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void deleteApplication(UUID userId, UUID applicationId) {
        JobApplication application = getApplicationEntity(userId, applicationId);
        applicationRepository.delete(application);
    }

    private JobApplication getApplicationEntity(UUID userId, UUID applicationId) {
        return applicationRepository.findByIdAndUserId(applicationId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));
    }
}
