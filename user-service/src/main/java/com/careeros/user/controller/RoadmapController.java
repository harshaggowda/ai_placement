package com.careeros.user.controller;

import com.careeros.common.pagination.PaginationResponse;
import com.careeros.common.response.ApiResponse;
import com.careeros.security.AuthenticatedUser;
import com.careeros.security.CurrentUser;
import com.careeros.user.dto.*;
import com.careeros.user.service.RoadmapService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users/roadmaps")
@RequiredArgsConstructor
@Tag(name = "Roadmaps", description = "Manage the current user's roadmaps and tasks")
public class RoadmapController {

    private final RoadmapService roadmapService;

    @Operation(summary = "Create a roadmap")
    @PostMapping
    public ResponseEntity<ApiResponse<RoadmapResponseDto>> create(
            @CurrentUser AuthenticatedUser user, @Valid @RequestBody RoadmapCreateDto request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(roadmapService.create(user.userId(), request), "Roadmap created"));
    }

    @Operation(summary = "Get a roadmap by id")
    @GetMapping("/{roadmapId}")
    public ResponseEntity<ApiResponse<RoadmapResponseDto>> get(
            @CurrentUser AuthenticatedUser user, @PathVariable UUID roadmapId) {
        return ResponseEntity.ok(ApiResponse.success(roadmapService.get(user.userId(), roadmapId)));
    }

    @Operation(summary = "List the current user's roadmaps")
    @GetMapping
    public ResponseEntity<ApiResponse<PaginationResponse<RoadmapResponseDto>>> list(
            @CurrentUser AuthenticatedUser user, Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(roadmapService.list(user.userId(), pageable)));
    }

    @Operation(summary = "List roadmap templates")
    @GetMapping("/templates")
    public ResponseEntity<ApiResponse<PaginationResponse<RoadmapResponseDto>>> listTemplates(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(roadmapService.listTemplates(pageable)));
    }

    @Operation(summary = "Update a roadmap")
    @PutMapping("/{roadmapId}")
    public ResponseEntity<ApiResponse<RoadmapResponseDto>> update(
            @CurrentUser AuthenticatedUser user, @PathVariable UUID roadmapId,
            @Valid @RequestBody RoadmapUpdateDto request) {
        return ResponseEntity.ok(ApiResponse.success(
                roadmapService.update(user.userId(), roadmapId, request), "Roadmap updated"));
    }

    @Operation(summary = "Soft-delete a roadmap")
    @DeleteMapping("/{roadmapId}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @CurrentUser AuthenticatedUser user, @PathVariable UUID roadmapId) {
        roadmapService.delete(user.userId(), roadmapId);
        return ResponseEntity.ok(ApiResponse.ok("Roadmap deleted"));
    }

    @Operation(summary = "Add a task to a roadmap")
    @PostMapping("/{roadmapId}/tasks")
    public ResponseEntity<ApiResponse<RoadmapTaskResponseDto>> addTask(
            @CurrentUser AuthenticatedUser user, @PathVariable UUID roadmapId,
            @Valid @RequestBody RoadmapTaskCreateDto request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(roadmapService.addTask(user.userId(), roadmapId, request), "Task added"));
    }

    @Operation(summary = "List tasks in a roadmap")
    @GetMapping("/{roadmapId}/tasks")
    public ResponseEntity<ApiResponse<List<RoadmapTaskResponseDto>>> listTasks(
            @CurrentUser AuthenticatedUser user, @PathVariable UUID roadmapId) {
        return ResponseEntity.ok(ApiResponse.success(roadmapService.listTasks(user.userId(), roadmapId)));
    }

    @Operation(summary = "Update a roadmap task")
    @PutMapping("/{roadmapId}/tasks/{taskId}")
    public ResponseEntity<ApiResponse<RoadmapTaskResponseDto>> updateTask(
            @CurrentUser AuthenticatedUser user, @PathVariable UUID roadmapId, @PathVariable UUID taskId,
            @Valid @RequestBody RoadmapTaskUpdateDto request) {
        return ResponseEntity.ok(ApiResponse.success(
                roadmapService.updateTask(user.userId(), roadmapId, taskId, request), "Task updated"));
    }

    @Operation(summary = "Soft-delete a roadmap task")
    @DeleteMapping("/{roadmapId}/tasks/{taskId}")
    public ResponseEntity<ApiResponse<Void>> deleteTask(
            @CurrentUser AuthenticatedUser user, @PathVariable UUID roadmapId, @PathVariable UUID taskId) {
        roadmapService.deleteTask(user.userId(), roadmapId, taskId);
        return ResponseEntity.ok(ApiResponse.ok("Task deleted"));
    }
}
