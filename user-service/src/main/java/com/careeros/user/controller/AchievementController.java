package com.careeros.user.controller;

import com.careeros.common.pagination.PaginationResponse;
import com.careeros.common.response.ApiResponse;
import com.careeros.security.AuthenticatedUser;
import com.careeros.security.CurrentUser;
import com.careeros.user.dto.UserAchievementResponseDto;
import com.careeros.user.service.AchievementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/achievements")
@RequiredArgsConstructor
@Tag(name = "Achievements", description = "View unlocked achievements")
public class AchievementController {

    private final AchievementService achievementService;

    @Operation(summary = "List the current user's achievements")
    @GetMapping
    public ResponseEntity<ApiResponse<PaginationResponse<UserAchievementResponseDto>>> list(
            @CurrentUser AuthenticatedUser user, Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(achievementService.list(user.userId(), pageable)));
    }
}
