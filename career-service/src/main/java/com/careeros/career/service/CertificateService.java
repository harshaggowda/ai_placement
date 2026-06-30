package com.careeros.career.service;

import com.careeros.career.dto.CertificateDto;
import com.careeros.career.dto.CertificateResponseDto;

import java.util.List;
import java.util.UUID;

public interface CertificateService {

    List<CertificateResponseDto> getAllCertificates(UUID userId);

    CertificateResponseDto getCertificate(UUID userId, UUID certificateId);

    CertificateResponseDto createCertificate(UUID userId, CertificateDto dto);

    CertificateResponseDto updateCertificate(UUID userId, UUID certificateId, CertificateDto dto);

    void deleteCertificate(UUID userId, UUID certificateId);
}
