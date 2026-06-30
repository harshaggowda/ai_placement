package com.careeros.career.controller;

import com.careeros.career.dto.ProgressSnapshotDto;
import com.careeros.career.dto.ProgressSnapshotResponseDto;
import com.careeros.career.service.ProgressSnapshotService;
import com.careeros.common.response.ApiResponse;
import com.careeros.security.AuthenticatedUser;
import com.careeros.security.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/career/snapshots")
@RequiredArgsConstructor
@Tag(name = "Progress Snapshot", description = "Endpoints for managing user progress snapshots")
public class ProgressSnapshotController {

    private final ProgressSnapshotService snapshotService;

    @GetMapping
    @Operation(summary = "Get all progress snapshots for the authenticated user")
    public ResponseEntity<ApiResponse<List<ProgressSnapshotResponseDto>>> getAllSnapshots(
            @CurrentUser AuthenticatedUser user) {
        return ResponseEntity.ok(ApiResponse.success(snapshotService.getAllSnapshots(user.userId())));
    }

    @GetMapping("/{snapshotId}")
    @Operation(summary = "Get a specific progress snapshot")
    public ResponseEntity<ApiResponse<ProgressSnapshotResponseDto>> getSnapshot(
            @CurrentUser AuthenticatedUser user,
            @PathVariable UUID snapshotId) {
        return ResponseEntity.ok(ApiResponse.success(snapshotService.getSnapshot(user.userId(), snapshotId)));
    }
    
    @GetMapping("/date/{snapshotDate}")
    @Operation(summary = "Get a progress snapshot by date")
    public ResponseEntity<ApiResponse<ProgressSnapshotResponseDto>> getSnapshotByDate(
            @CurrentUser AuthenticatedUser user,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate snapshotDate) {
        return ResponseEntity.ok(ApiResponse.success(snapshotService.getSnapshotByDate(user.userId(), snapshotDate)));
    }

    @PostMapping
    @Operation(summary = "Create a new progress snapshot")
    public ResponseEntity<ApiResponse<ProgressSnapshotResponseDto>> createSnapshot(
            @CurrentUser AuthenticatedUser user,
            @Valid @RequestBody ProgressSnapshotDto dto) {
        ProgressSnapshotResponseDto created = snapshotService.createSnapshot(user.userId(), dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(created));
    }

    @PutMapping("/{snapshotId}")
    @Operation(summary = "Update an existing progress snapshot")
    public ResponseEntity<ApiResponse<ProgressSnapshotResponseDto>> updateSnapshot(
            @CurrentUser AuthenticatedUser user,
            @PathVariable UUID snapshotId,
            @Valid @RequestBody ProgressSnapshotDto dto) {
        return ResponseEntity.ok(ApiResponse.success(snapshotService.updateSnapshot(user.userId(), snapshotId, dto)));
    }

    @DeleteMapping("/{snapshotId}")
    @Operation(summary = "Delete a progress snapshot")
    public ResponseEntity<ApiResponse<Void>> deleteSnapshot(
            @CurrentUser AuthenticatedUser user,
            @PathVariable UUID snapshotId) {
        snapshotService.deleteSnapshot(user.userId(), snapshotId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
