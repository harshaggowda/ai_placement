package com.careeros.career.service.impl;

import com.careeros.career.dto.JobApplicationDto;
import com.careeros.career.dto.JobApplicationResponseDto;
import com.careeros.career.entity.ApplicationStatus;
import com.careeros.career.entity.Company;
import com.careeros.career.entity.JobApplication;
import com.careeros.career.mapper.JobApplicationMapper;
import com.careeros.career.repository.CompanyRepository;
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
class JobApplicationServiceImplTest {

    @Mock JobApplicationRepository applicationRepository;
    @Mock CompanyRepository companyRepository;
    @Mock JobApplicationMapper applicationMapper;

    @InjectMocks JobApplicationServiceImpl applicationService;

    private final UUID userId = UUID.randomUUID();
    private final UUID companyId = UUID.randomUUID();
    private final UUID applicationId = UUID.randomUUID();

    @Test
    void createApplicationSucceeds() {
        JobApplicationDto dto = new JobApplicationDto(companyId, null, "Software Engineer", null, null, null, null, ApplicationStatus.APPLIED);
        Company company = new Company();
        company.setId(companyId);
        
        JobApplication application = new JobApplication();
        application.setId(applicationId);

        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(applicationMapper.toEntity(dto)).thenReturn(application);
        when(applicationRepository.save(application)).thenReturn(application);
        
        JobApplicationResponseDto responseMock = new JobApplicationResponseDto(
                applicationId, userId, null, null, "Software Engineer", null, null, null, null, ApplicationStatus.APPLIED, null, null, null);
        when(applicationMapper.toResponse(application)).thenReturn(responseMock);

        JobApplicationResponseDto result = applicationService.createApplication(userId, dto);

        verify(applicationRepository).save(application);
        assertThat(result.role()).isEqualTo("Software Engineer");
    }

    @Test
    void getApplicationThrowsWhenNotOwned() {
        when(applicationRepository.findByIdAndUserId(applicationId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> applicationService.getApplication(userId, applicationId))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
