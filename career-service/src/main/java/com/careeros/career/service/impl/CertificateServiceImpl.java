package com.careeros.career.service.impl;

import com.careeros.career.dto.CertificateDto;
import com.careeros.career.dto.CertificateResponseDto;
import com.careeros.career.entity.Certificate;
import com.careeros.career.mapper.CertificateMapper;
import com.careeros.career.repository.CertificateRepository;
import com.careeros.career.service.CertificateService;
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
public class CertificateServiceImpl implements CertificateService {

    private final CertificateRepository certificateRepository;
    private final CertificateMapper certificateMapper;

    @Override
    @Transactional(readOnly = true)
    public List<CertificateResponseDto> getAllCertificates(UUID userId) {
        return certificateRepository.findAllByUserId(userId).stream()
                .map(certificateMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CertificateResponseDto getCertificate(UUID userId, UUID certificateId) {
        Certificate certificate = getCertificateEntity(userId, certificateId);
        return certificateMapper.toResponse(certificate);
    }

    @Override
    @Transactional
    public CertificateResponseDto createCertificate(UUID userId, CertificateDto dto) {
        Certificate certificate = certificateMapper.toEntity(dto);
        certificate.setUserId(userId);

        Certificate saved = certificateRepository.save(certificate);
        return certificateMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public CertificateResponseDto updateCertificate(UUID userId, UUID certificateId, CertificateDto dto) {
        Certificate certificate = getCertificateEntity(userId, certificateId);
        certificateMapper.updateEntity(dto, certificate);

        Certificate updated = certificateRepository.save(certificate);
        return certificateMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void deleteCertificate(UUID userId, UUID certificateId) {
        Certificate certificate = getCertificateEntity(userId, certificateId);
        certificateRepository.delete(certificate);
    }

    private Certificate getCertificateEntity(UUID userId, UUID certificateId) {
        return certificateRepository.findByIdAndUserId(certificateId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Certificate not found"));
    }
}
