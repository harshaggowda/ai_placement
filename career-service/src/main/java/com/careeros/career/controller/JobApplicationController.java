package com.careeros.career.controller;

import com.careeros.career.dto.JobApplicationDto;
import com.careeros.career.dto.JobApplicationResponseDto;
import com.careeros.career.service.JobApplicationService;
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
@RequestMapping("/api/career/applications")
@RequiredArgsConstructor
@Tag(name = "Job Applications", description = "Endpoints for managing user job applications")
public class JobApplicationController {

    private final JobApplicationService applicationService;

    @GetMapping
    @Operation(summary = "Get all job applications for the authenticated user")
    public ResponseEntity<ApiResponse<List<JobApplicationResponseDto>>> getAllApplications(
            @CurrentUser AuthenticatedUser user) {
        return ResponseEntity.ok(ApiResponse.success(applicationService.getAllApplications(user.userId())));
    }

    @GetMapping("/{applicationId}")
    @Operation(summary = "Get a specific job application")
    public ResponseEntity<ApiResponse<JobApplicationResponseDto>> getApplication(
            @CurrentUser AuthenticatedUser user,
            @PathVariable UUID applicationId) {
        return ResponseEntity.ok(ApiResponse.success(applicationService.getApplication(user.userId(), applicationId)));
    }

    @PostMapping
    @Operation(summary = "Create a new job application")
    public ResponseEntity<ApiResponse<JobApplicationResponseDto>> createApplication(
            @CurrentUser AuthenticatedUser user,
            @Valid @RequestBody JobApplicationDto dto) {
        JobApplicationResponseDto created = applicationService.createApplication(user.userId(), dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(created));
    }

    @PutMapping("/{applicationId}")
    @Operation(summary = "Update an existing job application")
    public ResponseEntity<ApiResponse<JobApplicationResponseDto>> updateApplication(
            @CurrentUser AuthenticatedUser user,
            @PathVariable UUID applicationId,
            @Valid @RequestBody JobApplicationDto dto) {
        return ResponseEntity.ok(ApiResponse.success(applicationService.updateApplication(user.userId(), applicationId, dto)));
    }

    @DeleteMapping("/{applicationId}")
    @Operation(summary = "Delete a job application")
    public ResponseEntity<ApiResponse<Void>> deleteApplication(
            @CurrentUser AuthenticatedUser user,
            @PathVariable UUID applicationId) {
        applicationService.deleteApplication(user.userId(), applicationId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
