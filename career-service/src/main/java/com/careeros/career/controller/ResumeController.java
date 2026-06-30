package com.careeros.career.controller;

import com.careeros.career.dto.ResumeAnalysisMetadataDto;
import com.careeros.career.dto.ResumeAnalysisMetadataResponseDto;
import com.careeros.career.dto.ResumeDto;
import com.careeros.career.dto.ResumeResponseDto;
import com.careeros.career.service.ResumeService;
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
@RequestMapping("/api/career/resumes")
@RequiredArgsConstructor
@Tag(name = "Resumes", description = "Endpoints for managing user resumes and analysis metadata")
public class ResumeController {

    private final ResumeService resumeService;

    @GetMapping
    @Operation(summary = "Get all resumes for the authenticated user")
    public ResponseEntity<ApiResponse<List<ResumeResponseDto>>> getAllResumes(@CurrentUser AuthenticatedUser user) {
        return ResponseEntity.ok(ApiResponse.success(resumeService.getAllResumes(user.userId())));
    }

    @GetMapping("/{resumeId}")
    @Operation(summary = "Get a specific resume")
    public ResponseEntity<ApiResponse<ResumeResponseDto>> getResume(
            @CurrentUser AuthenticatedUser user,
            @PathVariable UUID resumeId) {
        return ResponseEntity.ok(ApiResponse.success(resumeService.getResume(user.userId(), resumeId)));
    }

    @PostMapping
    @Operation(summary = "Create a new resume")
    public ResponseEntity<ApiResponse<ResumeResponseDto>> createResume(
            @CurrentUser AuthenticatedUser user,
            @Valid @RequestBody ResumeDto dto) {
        ResumeResponseDto created = resumeService.createResume(user.userId(), dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(created));
    }

    @PutMapping("/{resumeId}")
    @Operation(summary = "Update an existing resume version")
    public ResponseEntity<ApiResponse<ResumeResponseDto>> updateResume(
            @CurrentUser AuthenticatedUser user,
            @PathVariable UUID resumeId,
            @Valid @RequestBody ResumeDto dto) {
        return ResponseEntity.ok(ApiResponse.success(resumeService.updateResume(user.userId(), resumeId, dto)));
    }

    @PostMapping("/{resumeId}/versions")
    @Operation(summary = "Create a new version of an existing resume")
    public ResponseEntity<ApiResponse<ResumeResponseDto>> createNewVersion(
            @CurrentUser AuthenticatedUser user,
            @PathVariable UUID resumeId,
            @Valid @RequestBody ResumeDto dto) {
        ResumeResponseDto newVersion = resumeService.createNewVersion(user.userId(), resumeId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(newVersion));
    }

    @DeleteMapping("/{resumeId}")
    @Operation(summary = "Soft delete a resume")
    public ResponseEntity<ApiResponse<Void>> deleteResume(
            @CurrentUser AuthenticatedUser user,
            @PathVariable UUID resumeId) {
        resumeService.deleteResume(user.userId(), resumeId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // --- Metadata endpoints ---

    @GetMapping("/{resumeId}/analysis-metadata")
    @Operation(summary = "Get AI analysis metadata for a resume")
    public ResponseEntity<ApiResponse<ResumeAnalysisMetadataResponseDto>> getAnalysisMetadata(
            @CurrentUser AuthenticatedUser user,
            @PathVariable UUID resumeId) {
        return ResponseEntity.ok(ApiResponse.success(resumeService.getAnalysisMetadata(user.userId(), resumeId)));
    }

    @PostMapping("/{resumeId}/analysis-metadata")
    @Operation(summary = "Save or update AI analysis metadata")
    public ResponseEntity<ApiResponse<ResumeAnalysisMetadataResponseDto>> saveAnalysisMetadata(
            @CurrentUser AuthenticatedUser user,
            @PathVariable UUID resumeId,
            @Valid @RequestBody ResumeAnalysisMetadataDto dto) {
        return ResponseEntity.ok(ApiResponse.success(resumeService.saveAnalysisMetadata(user.userId(), resumeId, dto)));
    }
}
