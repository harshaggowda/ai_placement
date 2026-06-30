package com.careeros.user.controller;

import com.careeros.common.response.ApiResponse;
import com.careeros.security.AuthenticatedUser;
import com.careeros.security.CurrentUser;
import com.careeros.user.dto.NotificationPreferenceDto;
import com.careeros.user.dto.NotificationPreferenceResponseDto;
import com.careeros.user.service.NotificationPreferenceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users/notification-preferences")
@RequiredArgsConstructor
@Tag(name = "Notification Preferences", description = "Manage notification settings")
public class NotificationPreferenceController {

    private final NotificationPreferenceService preferenceService;

    @Operation(summary = "Get the current user's notification preferences")
    @GetMapping
    public ResponseEntity<ApiResponse<NotificationPreferenceResponseDto>> get(@CurrentUser AuthenticatedUser user) {
        return ResponseEntity.ok(ApiResponse.success(preferenceService.get(user.userId())));
    }

    @Operation(summary = "Upsert the current user's notification preferences")
    @PutMapping
    public ResponseEntity<ApiResponse<NotificationPreferenceResponseDto>> upsert(
            @CurrentUser AuthenticatedUser user, @Valid @RequestBody NotificationPreferenceDto request) {
        return ResponseEntity.ok(ApiResponse.success(preferenceService.upsert(user.userId(), request), "Preferences updated"));
    }
}
