package com.careeros.career.controller;

import com.careeros.career.dto.DailyPlannerDto;
import com.careeros.career.dto.DailyPlannerResponseDto;
import com.careeros.career.service.DailyPlannerService;
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
@RequestMapping("/api/career/planners")
@RequiredArgsConstructor
@Tag(name = "Daily Planner", description = "Endpoints for managing user daily planners")
public class DailyPlannerController {

    private final DailyPlannerService dailyPlannerService;

    @GetMapping
    @Operation(summary = "Get all daily planners for the authenticated user")
    public ResponseEntity<ApiResponse<List<DailyPlannerResponseDto>>> getAllPlanners(
            @CurrentUser AuthenticatedUser user) {
        return ResponseEntity.ok(ApiResponse.success(dailyPlannerService.getAllPlanners(user.userId())));
    }

    @GetMapping("/{plannerId}")
    @Operation(summary = "Get a specific daily planner")
    public ResponseEntity<ApiResponse<DailyPlannerResponseDto>> getPlanner(
            @CurrentUser AuthenticatedUser user,
            @PathVariable UUID plannerId) {
        return ResponseEntity.ok(ApiResponse.success(dailyPlannerService.getPlanner(user.userId(), plannerId)));
    }
    
    @GetMapping("/date/{targetDate}")
    @Operation(summary = "Get a daily planner by date")
    public ResponseEntity<ApiResponse<DailyPlannerResponseDto>> getPlannerByDate(
            @CurrentUser AuthenticatedUser user,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate targetDate) {
        return ResponseEntity.ok(ApiResponse.success(dailyPlannerService.getPlannerByDate(user.userId(), targetDate)));
    }

    @PostMapping
    @Operation(summary = "Create a new daily planner")
    public ResponseEntity<ApiResponse<DailyPlannerResponseDto>> createPlanner(
            @CurrentUser AuthenticatedUser user,
            @Valid @RequestBody DailyPlannerDto dto) {
        DailyPlannerResponseDto created = dailyPlannerService.createPlanner(user.userId(), dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(created));
    }

    @PutMapping("/{plannerId}")
    @Operation(summary = "Update an existing daily planner")
    public ResponseEntity<ApiResponse<DailyPlannerResponseDto>> updatePlanner(
            @CurrentUser AuthenticatedUser user,
            @PathVariable UUID plannerId,
            @Valid @RequestBody DailyPlannerDto dto) {
        return ResponseEntity.ok(ApiResponse.success(dailyPlannerService.updatePlanner(user.userId(), plannerId, dto)));
    }

    @DeleteMapping("/{plannerId}")
    @Operation(summary = "Delete a daily planner")
    public ResponseEntity<ApiResponse<Void>> deletePlanner(
            @CurrentUser AuthenticatedUser user,
            @PathVariable UUID plannerId) {
        dailyPlannerService.deletePlanner(user.userId(), plannerId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
