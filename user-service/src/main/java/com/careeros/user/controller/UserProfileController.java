package com.careeros.user.controller;

import com.careeros.common.response.ApiResponse;
import com.careeros.security.AuthenticatedUser;
import com.careeros.security.CurrentUser;
import com.careeros.user.dto.ProfileCreateDto;
import com.careeros.user.dto.ProfileResponseDto;
import com.careeros.user.dto.ProfileUpdateDto;
import com.careeros.user.service.UserProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Current user's profile. Every operation is implicitly scoped to the authenticated user (resolved
 * via {@link CurrentUser}), so a user can only ever read/modify their own profile.
 */
@RestController
@RequestMapping("/api/users/profile")
@RequiredArgsConstructor
@Tag(name = "User Profile", description = "Manage the authenticated user's profile")
public class UserProfileController {

    private final UserProfileService profileService;

    @Operation(summary = "Create the current user's profile")
    @PostMapping
    public ResponseEntity<ApiResponse<ProfileResponseDto>> create(
            @CurrentUser AuthenticatedUser user, @Valid @RequestBody ProfileCreateDto request) {
        ProfileResponseDto profile = profileService.createProfile(user.userId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(profile, "Profile created"));
    }

    @Operation(summary = "Get the current user's profile")
    @GetMapping
    public ResponseEntity<ApiResponse<ProfileResponseDto>> get(@CurrentUser AuthenticatedUser user) {
        return ResponseEntity.ok(ApiResponse.success(profileService.getProfile(user.userId())));
    }

    @Operation(summary = "Replace the current user's profile")
    @PutMapping
    public ResponseEntity<ApiResponse<ProfileResponseDto>> replace(
            @CurrentUser AuthenticatedUser user, @Valid @RequestBody ProfileUpdateDto request) {
        return ResponseEntity.ok(ApiResponse.success(profileService.updateProfile(user.userId(), request), "Profile updated"));
    }

    @Operation(summary = "Partially update the current user's profile")
    @PatchMapping
    public ResponseEntity<ApiResponse<ProfileResponseDto>> patch(
            @CurrentUser AuthenticatedUser user, @Valid @RequestBody ProfileUpdateDto request) {
        return ResponseEntity.ok(ApiResponse.success(profileService.updateProfile(user.userId(), request), "Profile updated"));
    }

    @Operation(summary = "Soft-delete the current user's profile")
    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> delete(@CurrentUser AuthenticatedUser user) {
        profileService.deleteProfile(user.userId());
        return ResponseEntity.ok(ApiResponse.ok("Profile deleted"));
    }
}
