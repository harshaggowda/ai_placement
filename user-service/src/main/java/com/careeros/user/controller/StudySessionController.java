package com.careeros.user.controller;

import com.careeros.common.pagination.PaginationResponse;
import com.careeros.common.response.ApiResponse;
import com.careeros.security.AuthenticatedUser;
import com.careeros.security.CurrentUser;
import com.careeros.user.dto.StudySessionResponseDto;
import com.careeros.user.dto.StudySessionStartDto;
import com.careeros.user.dto.StudyStatsDto;
import com.careeros.user.service.StudySessionService;
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
@RequestMapping("/api/users/study-sessions")
@RequiredArgsConstructor
@Tag(name = "Study Sessions", description = "Track and manage study sessions")
public class StudySessionController {

    private final StudySessionService studySessionService;

    @Operation(summary = "Start a study session")
    @PostMapping("/start")
    public ResponseEntity<ApiResponse<StudySessionResponseDto>> start(
            @CurrentUser AuthenticatedUser user, @Valid @RequestBody StudySessionStartDto request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(studySessionService.start(user.userId(), request), "Session started"));
    }

    @Operation(summary = "Pause a study session")
    @PostMapping("/{sessionId}/pause")
    public ResponseEntity<ApiResponse<StudySessionResponseDto>> pause(
            @CurrentUser AuthenticatedUser user, @PathVariable UUID sessionId) {
        return ResponseEntity.ok(ApiResponse.success(studySessionService.pause(user.userId(), sessionId), "Session paused"));
    }

    @Operation(summary = "Resume a study session")
    @PostMapping("/{sessionId}/resume")
    public ResponseEntity<ApiResponse<StudySessionResponseDto>> resume(
            @CurrentUser AuthenticatedUser user, @PathVariable UUID sessionId) {
        return ResponseEntity.ok(ApiResponse.success(studySessionService.resume(user.userId(), sessionId), "Session resumed"));
    }

    @Operation(summary = "Finish a study session")
    @PostMapping("/{sessionId}/finish")
    public ResponseEntity<ApiResponse<StudySessionResponseDto>> finish(
            @CurrentUser AuthenticatedUser user, @PathVariable UUID sessionId) {
        return ResponseEntity.ok(ApiResponse.success(studySessionService.finish(user.userId(), sessionId), "Session completed"));
    }

    @Operation(summary = "Get a study session")
    @GetMapping("/{sessionId}")
    public ResponseEntity<ApiResponse<StudySessionResponseDto>> get(
            @CurrentUser AuthenticatedUser user, @PathVariable UUID sessionId) {
        return ResponseEntity.ok(ApiResponse.success(studySessionService.get(user.userId(), sessionId)));
    }

    @Operation(summary = "List study sessions")
    @GetMapping
    public ResponseEntity<ApiResponse<PaginationResponse<StudySessionResponseDto>>> list(
            @CurrentUser AuthenticatedUser user, Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(studySessionService.list(user.userId(), pageable)));
    }

    @Operation(summary = "Get study statistics")
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<StudyStatsDto>> stats(@CurrentUser AuthenticatedUser user) {
        return ResponseEntity.ok(ApiResponse.success(studySessionService.stats(user.userId())));
    }
}
