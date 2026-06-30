package com.careeros.career.controller;

import com.careeros.career.dto.CompanyDto;
import com.careeros.career.dto.CompanyResponseDto;
import com.careeros.career.service.CompanyService;
import com.careeros.common.pagination.PaginationResponse;
import com.careeros.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/career/companies")
@RequiredArgsConstructor
@Tag(name = "Companies", description = "Endpoints for managing the company catalog (system-wide data)")
public class CompanyController {

    private final CompanyService companyService;

    @GetMapping
    @Operation(summary = "Search companies with filters")
    public ResponseEntity<ApiResponse<PaginationResponse<CompanyResponseDto>>> searchCompanies(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String industry,
            @RequestParam(required = false) String hiringStatus,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(
                PaginationResponse.from(companyService.searchCompanies(name, industry, hiringStatus, pageable))
        ));
    }

    @GetMapping("/{companyId}")
    @Operation(summary = "Get company details")
    public ResponseEntity<ApiResponse<CompanyResponseDto>> getCompany(@PathVariable UUID companyId) {
        return ResponseEntity.ok(ApiResponse.success(companyService.getCompany(companyId)));
    }

    @PostMapping
    @Operation(summary = "Create a new company in the catalog (Admin/AI integration)")
    public ResponseEntity<ApiResponse<CompanyResponseDto>> createCompany(@Valid @RequestBody CompanyDto dto) {
        CompanyResponseDto created = companyService.createCompany(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(created));
    }

    @PutMapping("/{companyId}")
    @Operation(summary = "Update an existing company")
    public ResponseEntity<ApiResponse<CompanyResponseDto>> updateCompany(
            @PathVariable UUID companyId,
            @Valid @RequestBody CompanyDto dto) {
        return ResponseEntity.ok(ApiResponse.success(companyService.updateCompany(companyId, dto)));
    }
}
