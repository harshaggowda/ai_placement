package com.careeros.career.controller;

import com.careeros.career.dto.ProjectDto;
import com.careeros.career.dto.ProjectResponseDto;
import com.careeros.career.service.ProjectService;
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
@RequestMapping("/api/career/projects")
@RequiredArgsConstructor
@Tag(name = "Projects", description = "Endpoints for managing user portfolio projects")
public class ProjectController {

    private final ProjectService projectService;

    @GetMapping
    @Operation(summary = "Get all projects for the authenticated user")
    public ResponseEntity<ApiResponse<List<ProjectResponseDto>>> getAllProjects(
            @CurrentUser AuthenticatedUser user) {
        return ResponseEntity.ok(ApiResponse.success(projectService.getAllProjects(user.userId())));
    }

    @GetMapping("/{projectId}")
    @Operation(summary = "Get a specific project")
    public ResponseEntity<ApiResponse<ProjectResponseDto>> getProject(
            @CurrentUser AuthenticatedUser user,
            @PathVariable UUID projectId) {
        return ResponseEntity.ok(ApiResponse.success(projectService.getProject(user.userId(), projectId)));
    }

    @PostMapping
    @Operation(summary = "Create a new project")
    public ResponseEntity<ApiResponse<ProjectResponseDto>> createProject(
            @CurrentUser AuthenticatedUser user,
            @Valid @RequestBody ProjectDto dto) {
        ProjectResponseDto created = projectService.createProject(user.userId(), dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(created));
    }

    @PutMapping("/{projectId}")
    @Operation(summary = "Update an existing project")
    public ResponseEntity<ApiResponse<ProjectResponseDto>> updateProject(
            @CurrentUser AuthenticatedUser user,
            @PathVariable UUID projectId,
            @Valid @RequestBody ProjectDto dto) {
        return ResponseEntity.ok(ApiResponse.success(projectService.updateProject(user.userId(), projectId, dto)));
    }

    @DeleteMapping("/{projectId}")
    @Operation(summary = "Delete a project")
    public ResponseEntity<ApiResponse<Void>> deleteProject(
            @CurrentUser AuthenticatedUser user,
            @PathVariable UUID projectId) {
        projectService.deleteProject(user.userId(), projectId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
