package com.careeros.career.controller;

import com.careeros.career.dto.CompanyPreparationDto;
import com.careeros.career.dto.CompanyPreparationResponseDto;
import com.careeros.career.service.CompanyService;
import com.careeros.common.response.ApiResponse;
import com.careeros.security.AuthenticatedUser;
import com.careeros.security.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/career/preparations")
@RequiredArgsConstructor
@Tag(name = "Company Preparations", description = "Endpoints for managing user-specific company preparation progress")
public class CompanyPreparationController {

    private final CompanyService companyService;

    @GetMapping
    @Operation(summary = "Get all company preparations for the authenticated user")
    public ResponseEntity<ApiResponse<List<CompanyPreparationResponseDto>>> getAllPreparations(
            @CurrentUser AuthenticatedUser user) {
        return ResponseEntity.ok(ApiResponse.success(companyService.getAllPreparations(user.userId())));
    }

    @GetMapping("/{companyId}")
    @Operation(summary = "Get a specific company preparation")
    public ResponseEntity<ApiResponse<CompanyPreparationResponseDto>> getPreparation(
            @CurrentUser AuthenticatedUser user,
            @PathVariable UUID companyId) {
        return ResponseEntity.ok(ApiResponse.success(companyService.getPreparation(user.userId(), companyId)));
    }

    @PostMapping
    @Operation(summary = "Upsert (create or update) a company preparation")
    public ResponseEntity<ApiResponse<CompanyPreparationResponseDto>> upsertPreparation(
            @CurrentUser AuthenticatedUser user,
            @Valid @RequestBody CompanyPreparationDto dto) {
        return ResponseEntity.ok(ApiResponse.success(companyService.upsertPreparation(user.userId(), dto)));
    }

    @DeleteMapping("/{companyId}")
    @Operation(summary = "Delete a company preparation")
    public ResponseEntity<ApiResponse<Void>> deletePreparation(
            @CurrentUser AuthenticatedUser user,
            @PathVariable UUID companyId) {
        companyService.deletePreparation(user.userId(), companyId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
