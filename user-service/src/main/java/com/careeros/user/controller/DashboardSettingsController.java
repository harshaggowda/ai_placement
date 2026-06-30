package com.careeros.user.controller;

import com.careeros.common.response.ApiResponse;
import com.careeros.security.AuthenticatedUser;
import com.careeros.security.CurrentUser;
import com.careeros.user.dto.DashboardSettingsDto;
import com.careeros.user.dto.DashboardSettingsResponseDto;
import com.careeros.user.service.DashboardSettingsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users/dashboard-settings")
@RequiredArgsConstructor
@Tag(name = "Dashboard Settings", description = "Manage dashboard personalization")
public class DashboardSettingsController {

    private final DashboardSettingsService settingsService;

    @Operation(summary = "Get the current user's dashboard settings")
    @GetMapping
    public ResponseEntity<ApiResponse<DashboardSettingsResponseDto>> get(@CurrentUser AuthenticatedUser user) {
        return ResponseEntity.ok(ApiResponse.success(settingsService.get(user.userId())));
    }

    @Operation(summary = "Upsert the current user's dashboard settings")
    @PutMapping
    public ResponseEntity<ApiResponse<DashboardSettingsResponseDto>> upsert(
            @CurrentUser AuthenticatedUser user, @Valid @RequestBody DashboardSettingsDto request) {
        return ResponseEntity.ok(ApiResponse.success(settingsService.upsert(user.userId(), request), "Settings updated"));
    }
}
