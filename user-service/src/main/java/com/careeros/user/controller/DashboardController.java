package com.careeros.user.controller;

import com.careeros.common.response.ApiResponse;
import com.careeros.security.AuthenticatedUser;
import com.careeros.security.CurrentUser;
import com.careeros.user.dto.DashboardSummaryDto;
import com.careeros.user.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Aggregated dashboard data")
public class DashboardController {

    private final DashboardService dashboardService;

    @Operation(summary = "Get the current user's dashboard summary")
    @GetMapping
    public ResponseEntity<ApiResponse<DashboardSummaryDto>> getSummary(@CurrentUser AuthenticatedUser user) {
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getSummary(user.userId())));
    }
}
