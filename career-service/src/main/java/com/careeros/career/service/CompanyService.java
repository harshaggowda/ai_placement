package com.careeros.career.service;

import com.careeros.career.dto.CompanyDto;
import com.careeros.career.dto.CompanyPreparationDto;
import com.careeros.career.dto.CompanyPreparationResponseDto;
import com.careeros.career.dto.CompanyResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface CompanyService {

    // --- Company Catalog ---
    Page<CompanyResponseDto> searchCompanies(String name, String industry, String hiringStatus, Pageable pageable);

    CompanyResponseDto getCompany(UUID companyId);

    // AI/Admin capabilities to add/update companies
    CompanyResponseDto createCompany(CompanyDto dto);

    CompanyResponseDto updateCompany(UUID companyId, CompanyDto dto);


    // --- Company Preparation ---
    List<CompanyPreparationResponseDto> getAllPreparations(UUID userId);

    CompanyPreparationResponseDto getPreparation(UUID userId, UUID companyId);

    CompanyPreparationResponseDto upsertPreparation(UUID userId, CompanyPreparationDto dto);

    void deletePreparation(UUID userId, UUID companyId);
}
