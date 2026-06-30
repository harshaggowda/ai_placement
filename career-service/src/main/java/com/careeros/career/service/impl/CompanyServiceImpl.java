package com.careeros.career.service.impl;

import com.careeros.career.dto.CompanyDto;
import com.careeros.career.dto.CompanyPreparationDto;
import com.careeros.career.dto.CompanyPreparationResponseDto;
import com.careeros.career.dto.CompanyResponseDto;
import com.careeros.career.entity.Company;
import com.careeros.career.entity.CompanyPreparation;
import com.careeros.career.entity.HiringStatus;
import com.careeros.career.mapper.CompanyMapper;
import com.careeros.career.mapper.CompanyPreparationMapper;
import com.careeros.career.repository.CompanyPreparationRepository;
import com.careeros.career.repository.CompanyRepository;
import com.careeros.career.service.CompanyService;
import com.careeros.exception.ResourceNotFoundException;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompanyServiceImpl implements CompanyService {

    private final CompanyRepository companyRepository;
    private final CompanyPreparationRepository preparationRepository;
    private final CompanyMapper companyMapper;
    private final CompanyPreparationMapper preparationMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<CompanyResponseDto> searchCompanies(String name, String industry, String hiringStatus, Pageable pageable) {
        Specification<Company> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (StringUtils.hasText(name)) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
            }
            if (StringUtils.hasText(industry)) {
                predicates.add(cb.equal(root.get("industry"), industry));
            }
            if (StringUtils.hasText(hiringStatus)) {
                predicates.add(cb.equal(root.get("hiringStatus"), HiringStatus.valueOf(hiringStatus)));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return companyRepository.findAll(spec, pageable).map(companyMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public CompanyResponseDto getCompany(UUID companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found"));
        return companyMapper.toResponse(company);
    }

    @Override
    @Transactional
    public CompanyResponseDto createCompany(CompanyDto dto) {
        Company company = companyMapper.toEntity(dto);
        return companyMapper.toResponse(companyRepository.save(company));
    }

    @Override
    @Transactional
    public CompanyResponseDto updateCompany(UUID companyId, CompanyDto dto) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found"));
        companyMapper.updateEntity(dto, company);
        return companyMapper.toResponse(company);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompanyPreparationResponseDto> getAllPreparations(UUID userId) {
        return preparationRepository.findAllByUserId(userId).stream()
                .map(preparationMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CompanyPreparationResponseDto getPreparation(UUID userId, UUID companyId) {
        CompanyPreparation preparation = preparationRepository.findByUserIdAndCompanyId(userId, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Preparation not found"));
        return preparationMapper.toResponse(preparation);
    }

    @Override
    @Transactional
    public CompanyPreparationResponseDto upsertPreparation(UUID userId, CompanyPreparationDto dto) {
        Company company = companyRepository.findById(dto.companyId())
                .orElseThrow(() -> new ResourceNotFoundException("Company not found"));

        CompanyPreparation preparation = preparationRepository.findByUserIdAndCompanyId(userId, dto.companyId())
                .orElseGet(() -> {
                    CompanyPreparation p = new CompanyPreparation();
                    p.setUserId(userId);
                    p.setCompany(company);
                    return p;
                });

        preparationMapper.updateEntity(dto, preparation);
        CompanyPreparation saved = preparationRepository.save(preparation);

        return preparationMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void deletePreparation(UUID userId, UUID companyId) {
        CompanyPreparation preparation = preparationRepository.findByUserIdAndCompanyId(userId, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Preparation not found"));
        preparationRepository.delete(preparation);
    }
}
