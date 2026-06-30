package com.careeros.career.controller;

import com.careeros.career.dto.ResumeAnalysisMetadataDto;
import com.careeros.career.dto.ResumeAnalysisMetadataResponseDto;
import com.careeros.career.dto.ResumeResponseDto;
import com.careeros.career.service.ResumeService;
import com.careeros.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/internal/career")
@RequiredArgsConstructor
@Tag(name = "Internal API", description = "Internal APIs for AI Service consumption")
public class InternalCareerController {

    private final ResumeService resumeService;

    @GetMapping("/users/{userId}/resumes/active")
    @Operation(summary = "Get the active (latest) resume for a user")
    public ResponseEntity<ApiResponse<ResumeResponseDto>> getActiveResume(@PathVariable UUID userId) {
        // AI service needs the most recent resume. We'll fetch all and get the first one.
        List<ResumeResponseDto> resumes = resumeService.getAllResumes(userId);
        if (resumes.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(ApiResponse.success(resumes.get(0)));
    }

    @PostMapping("/users/{userId}/resumes/{resumeId}/metadata")
    @Operation(summary = "Save resume analysis metadata")
    public ResponseEntity<ApiResponse<ResumeAnalysisMetadataResponseDto>> saveAnalysisMetadata(
            @PathVariable UUID userId,
            @PathVariable UUID resumeId,
            @Valid @RequestBody ResumeAnalysisMetadataDto dto) {
        return ResponseEntity.ok(ApiResponse.success(resumeService.saveAnalysisMetadata(userId, resumeId, dto)));
    }
}
