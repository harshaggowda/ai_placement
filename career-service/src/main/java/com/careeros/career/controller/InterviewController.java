package com.careeros.career.controller;

import com.careeros.career.dto.InterviewDto;
import com.careeros.career.dto.InterviewResponseDto;
import com.careeros.career.service.InterviewService;
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
@RequestMapping("/api/career")
@RequiredArgsConstructor
@Tag(name = "Interviews", description = "Endpoints for managing user interviews")
public class InterviewController {

    private final InterviewService interviewService;

    @GetMapping("/interviews")
    @Operation(summary = "Get all interviews across all applications for the authenticated user")
    public ResponseEntity<ApiResponse<List<InterviewResponseDto>>> getAllInterviews(
            @CurrentUser AuthenticatedUser user) {
        return ResponseEntity.ok(ApiResponse.success(interviewService.getAllInterviews(user.userId())));
    }

    @GetMapping("/applications/{applicationId}/interviews")
    @Operation(summary = "Get all interviews for a specific application")
    public ResponseEntity<ApiResponse<List<InterviewResponseDto>>> getInterviewsForApplication(
            @CurrentUser AuthenticatedUser user,
            @PathVariable UUID applicationId) {
        return ResponseEntity.ok(ApiResponse.success(interviewService.getInterviewsForApplication(user.userId(), applicationId)));
    }

    @GetMapping("/interviews/{interviewId}")
    @Operation(summary = "Get a specific interview")
    public ResponseEntity<ApiResponse<InterviewResponseDto>> getInterview(
            @CurrentUser AuthenticatedUser user,
            @PathVariable UUID interviewId) {
        return ResponseEntity.ok(ApiResponse.success(interviewService.getInterview(user.userId(), interviewId)));
    }

    @PostMapping("/interviews")
    @Operation(summary = "Create a new interview")
    public ResponseEntity<ApiResponse<InterviewResponseDto>> createInterview(
            @CurrentUser AuthenticatedUser user,
            @Valid @RequestBody InterviewDto dto) {
        InterviewResponseDto created = interviewService.createInterview(user.userId(), dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(created));
    }

    @PutMapping("/interviews/{interviewId}")
    @Operation(summary = "Update an existing interview")
    public ResponseEntity<ApiResponse<InterviewResponseDto>> updateInterview(
            @CurrentUser AuthenticatedUser user,
            @PathVariable UUID interviewId,
            @Valid @RequestBody InterviewDto dto) {
        return ResponseEntity.ok(ApiResponse.success(interviewService.updateInterview(user.userId(), interviewId, dto)));
    }

    @DeleteMapping("/interviews/{interviewId}")
    @Operation(summary = "Delete an interview")
    public ResponseEntity<ApiResponse<Void>> deleteInterview(
            @CurrentUser AuthenticatedUser user,
            @PathVariable UUID interviewId) {
        interviewService.deleteInterview(user.userId(), interviewId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
