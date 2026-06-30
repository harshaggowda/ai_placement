package com.careeros.user.controller;

import com.careeros.common.pagination.PaginationResponse;
import com.careeros.common.response.ApiResponse;
import com.careeros.security.AuthenticatedUser;
import com.careeros.security.CurrentUser;
import com.careeros.user.dto.GoalCreateDto;
import com.careeros.user.dto.GoalResponseDto;
import com.careeros.user.dto.GoalSearchDto;
import com.careeros.user.dto.GoalSummaryDto;
import com.careeros.user.dto.GoalUpdateDto;
import com.careeros.user.service.GoalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * The current user's goals. All operations are ownership-scoped via
 * {@link CurrentUser}; a goal that
 * belongs to another user is indistinguishable from a non-existent one (404).
 */
@RestController
@RequestMapping("/api/users/goals")
@RequiredArgsConstructor
@Tag(name = "Goals", description = "Manage the authenticated user's goals")
public class GoalController {

    private final GoalService goalService;

    @Operation(summary = "Create a goal")
    @PostMapping
    public ResponseEntity<ApiResponse<GoalResponseDto>> create(
            @CurrentUser AuthenticatedUser user, @Valid @RequestBody GoalCreateDto request) {
        GoalResponseDto goal = goalService.create(user.userId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(goal, "Goal created"));
    }

    @Operation(summary = "Get a goal by id")
    @GetMapping("/{goalId}")
    public ResponseEntity<ApiResponse<GoalResponseDto>> get(
            @CurrentUser AuthenticatedUser user, @PathVariable UUID goalId) {
        return ResponseEntity.ok(ApiResponse.success(goalService.get(user.userId(), goalId)));
    }

    @Operation(summary = "List the current user's goals (paginated)")
    @GetMapping
    public ResponseEntity<ApiResponse<PaginationResponse<GoalSummaryDto>>> list(
            @CurrentUser AuthenticatedUser user, Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(goalService.list(user.userId(), pageable)));
    }

    @Operation(summary = "Search the current user's goals")
    @PostMapping("/search")
    public ResponseEntity<ApiResponse<PaginationResponse<GoalSummaryDto>>> search(
            @CurrentUser AuthenticatedUser user, @RequestBody GoalSearchDto criteria, Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(goalService.search(user.userId(), criteria, pageable)));
    }

    @Operation(summary = "Update a goal")
    @PutMapping("/{goalId}")
    public ResponseEntity<ApiResponse<GoalResponseDto>> update(
            @CurrentUser AuthenticatedUser user, @PathVariable UUID goalId,
            @Valid @RequestBody GoalUpdateDto request) {
        return ResponseEntity
                .ok(ApiResponse.success(goalService.update(user.userId(), goalId, request), "Goal updated"));
    }

    @Operation(summary = "Update a goal's progress (auto-completes at 100%)")
    @PatchMapping("/{goalId}/progress")
    public ResponseEntity<ApiResponse<GoalResponseDto>> updateProgress(
            @CurrentUser AuthenticatedUser user, @PathVariable UUID goalId, @RequestParam int percent) {
        return ResponseEntity.ok(ApiResponse.success(
                goalService.updateProgress(user.userId(), goalId, percent), "Progress updated"));
    }

    @Operation(summary = "Soft-delete a goal")
    @DeleteMapping("/{goalId}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @CurrentUser AuthenticatedUser user, @PathVariable UUID goalId) {
        goalService.delete(user.userId(), goalId);
        return ResponseEntity.ok(ApiResponse.ok("Goal deleted"));
    }
}
