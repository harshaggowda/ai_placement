package com.careeros.career.service.impl;

import com.careeros.career.dto.CompanyPreparationDto;
import com.careeros.career.dto.CompanyPreparationResponseDto;
import com.careeros.career.entity.Company;
import com.careeros.career.entity.CompanyPreparation;
import com.careeros.career.entity.PreparationStatus;
import com.careeros.career.mapper.CompanyMapper;
import com.careeros.career.mapper.CompanyPreparationMapper;
import com.careeros.career.repository.CompanyPreparationRepository;
import com.careeros.career.repository.CompanyRepository;
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
class CompanyServiceImplTest {

    @Mock CompanyRepository companyRepository;
    @Mock CompanyPreparationRepository preparationRepository;
    @Mock CompanyMapper companyMapper;
    @Mock CompanyPreparationMapper preparationMapper;

    @InjectMocks CompanyServiceImpl companyService;

    private final UUID userId = UUID.randomUUID();
    private final UUID companyId = UUID.randomUUID();

    @Test
    void upsertPreparationCreatesNewWhenNotExists() {
        CompanyPreparationDto dto = new CompanyPreparationDto(companyId, PreparationStatus.IN_PROGRESS, null, 10, null, null, null);
        Company company = new Company();
        company.setId(companyId);

        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(preparationRepository.findByUserIdAndCompanyId(userId, companyId)).thenReturn(Optional.empty());
        
        CompanyPreparation savedEntity = new CompanyPreparation();
        savedEntity.setUserId(userId);
        savedEntity.setCompany(company);
        savedEntity.setPreparationStatus(PreparationStatus.IN_PROGRESS);

        when(preparationRepository.save(any(CompanyPreparation.class))).thenReturn(savedEntity);
        
        CompanyPreparationResponseDto responseMock = new CompanyPreparationResponseDto(
                UUID.randomUUID(), userId, null, PreparationStatus.IN_PROGRESS, null, 10, null, null, null, null, null);
        when(preparationMapper.toResponse(savedEntity)).thenReturn(responseMock);

        CompanyPreparationResponseDto result = companyService.upsertPreparation(userId, dto);

        verify(preparationMapper).updateEntity(eq(dto), any(CompanyPreparation.class));
        verify(preparationRepository).save(any(CompanyPreparation.class));
        assertThat(result.preparationStatus()).isEqualTo(PreparationStatus.IN_PROGRESS);
    }

    @Test
    void upsertPreparationUpdatesExisting() {
        CompanyPreparationDto dto = new CompanyPreparationDto(companyId, PreparationStatus.READY, null, 100, null, null, null);
        Company company = new Company();
        company.setId(companyId);

        CompanyPreparation existing = new CompanyPreparation();
        existing.setUserId(userId);
        existing.setCompany(company);
        existing.setPreparationStatus(PreparationStatus.IN_PROGRESS);

        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(preparationRepository.findByUserIdAndCompanyId(userId, companyId)).thenReturn(Optional.of(existing));
        when(preparationRepository.save(existing)).thenReturn(existing);
        
        CompanyPreparationResponseDto responseMock = new CompanyPreparationResponseDto(
                existing.getId(), userId, null, PreparationStatus.READY, null, 100, null, null, null, null, null);
        when(preparationMapper.toResponse(existing)).thenReturn(responseMock);

        CompanyPreparationResponseDto result = companyService.upsertPreparation(userId, dto);

        verify(preparationMapper).updateEntity(dto, existing);
        verify(preparationRepository).save(existing);
        assertThat(result.preparationStatus()).isEqualTo(PreparationStatus.READY);
    }
    
    @Test
    void upsertPreparationThrowsWhenCompanyNotFound() {
        CompanyPreparationDto dto = new CompanyPreparationDto(companyId, PreparationStatus.IN_PROGRESS, null, 10, null, null, null);
        when(companyRepository.findById(companyId)).thenReturn(Optional.empty());
        
        assertThatThrownBy(() -> companyService.upsertPreparation(userId, dto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Company not found");
    }
}
