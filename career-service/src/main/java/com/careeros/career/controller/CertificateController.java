package com.careeros.career.controller;

import com.careeros.career.dto.CertificateDto;
import com.careeros.career.dto.CertificateResponseDto;
import com.careeros.career.service.CertificateService;
import com.careeros.common.response.ApiResponse;
import com.careeros.security.AuthenticatedUser;
import com.careeros.security.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/career/certificates")
@RequiredArgsConstructor
@Tag(name = "Certificates", description = "Endpoints for managing user certificates")
public class CertificateController {

    private final CertificateService certificateService;

    @GetMapping
    @Operation(summary = "Get all certificates for the authenticated user")
    public ResponseEntity<ApiResponse<List<CertificateResponseDto>>> getAllCertificates(
            @CurrentUser AuthenticatedUser user) {
        return ResponseEntity.ok(ApiResponse.success(certificateService.getAllCertificates(user.userId())));
    }

    @GetMapping("/{certificateId}")
    @Operation(summary = "Get a specific certificate")
    public ResponseEntity<ApiResponse<CertificateResponseDto>> getCertificate(
            @CurrentUser AuthenticatedUser user,
            @PathVariable UUID certificateId) {
        return ResponseEntity.ok(ApiResponse.success(certificateService.getCertificate(user.userId(), certificateId)));
    }

    @PostMapping
    @Operation(summary = "Create a new certificate")
    public ResponseEntity<ApiResponse<CertificateResponseDto>> createCertificate(
            @CurrentUser AuthenticatedUser user,
            @Valid @RequestBody CertificateDto dto) {
        CertificateResponseDto created = certificateService.createCertificate(user.userId(), dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(created));
    }

    @PutMapping("/{certificateId}")
    @Operation(summary = "Update an existing certificate")
    public ResponseEntity<ApiResponse<CertificateResponseDto>> updateCertificate(
            @CurrentUser AuthenticatedUser user,
            @PathVariable UUID certificateId,
            @Valid @RequestBody CertificateDto dto) {
        return ResponseEntity.ok(ApiResponse.success(certificateService.updateCertificate(user.userId(), certificateId, dto)));
    }

    @DeleteMapping("/{certificateId}")
    @Operation(summary = "Delete a certificate")
    public ResponseEntity<ApiResponse<Void>> deleteCertificate(
            @CurrentUser AuthenticatedUser user,
            @PathVariable UUID certificateId) {
        certificateService.deleteCertificate(user.userId(), certificateId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
