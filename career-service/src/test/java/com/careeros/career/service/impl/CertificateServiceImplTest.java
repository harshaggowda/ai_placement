package com.careeros.career.service.impl;

import com.careeros.career.dto.CertificateDto;
import com.careeros.career.dto.CertificateResponseDto;
import com.careeros.career.entity.Certificate;
import com.careeros.career.mapper.CertificateMapper;
import com.careeros.career.repository.CertificateRepository;
import com.careeros.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CertificateServiceImplTest {

    @Mock CertificateRepository certificateRepository;
    @Mock CertificateMapper certificateMapper;

    @InjectMocks CertificateServiceImpl certificateService;

    private final UUID userId = UUID.randomUUID();
    private final UUID certificateId = UUID.randomUUID();

    @Test
    void createCertificateSucceeds() {
        CertificateDto dto = new CertificateDto("AWS Certified Solutions Architect", "AWS", LocalDate.now(), null, null, "12345");
        
        Certificate cert = new Certificate();
        cert.setId(certificateId);

        when(certificateMapper.toEntity(dto)).thenReturn(cert);
        when(certificateRepository.save(cert)).thenReturn(cert);
        
        CertificateResponseDto responseMock = new CertificateResponseDto(certificateId, userId, "AWS Certified Solutions Architect", "AWS", LocalDate.now(), null, null, "12345", null, null);
        when(certificateMapper.toResponse(cert)).thenReturn(responseMock);

        CertificateResponseDto result = certificateService.createCertificate(userId, dto);

        verify(certificateRepository).save(cert);
        assertThat(result.issuer()).isEqualTo("AWS");
    }

    @Test
    void updateCertificateThrowsWhenNotOwned() {
        CertificateDto dto = new CertificateDto("AWS Certified Solutions Architect", "AWS", LocalDate.now(), null, null, "12345");

        when(certificateRepository.findByIdAndUserId(certificateId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> certificateService.updateCertificate(userId, certificateId, dto))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
