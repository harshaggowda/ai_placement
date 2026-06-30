package com.careeros.user.controller;

import com.careeros.common.response.ApiResponse;
import com.careeros.security.AuthenticatedUser;
import com.careeros.security.CurrentUser;
import com.careeros.user.dto.UserSkillAssignDto;
import com.careeros.user.dto.UserSkillResponseDto;
import com.careeros.user.dto.UserSkillUpdateDto;
import com.careeros.user.service.UserSkillService;
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
@RequestMapping("/api/users/my-skills")
@RequiredArgsConstructor
@Tag(name = "User Skills", description = "Manage the current user's skill assignments")
public class UserSkillController {

    private final UserSkillService userSkillService;

    @Operation(summary = "Assign a skill to the current user")
    @PostMapping
    public ResponseEntity<ApiResponse<UserSkillResponseDto>> assign(
            @CurrentUser AuthenticatedUser user, @Valid @RequestBody UserSkillAssignDto request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(userSkillService.assign(user.userId(), request), "Skill assigned"));
    }

    @Operation(summary = "Get a user skill assignment")
    @GetMapping("/{userSkillId}")
    public ResponseEntity<ApiResponse<UserSkillResponseDto>> get(
            @CurrentUser AuthenticatedUser user, @PathVariable UUID userSkillId) {
        return ResponseEntity.ok(ApiResponse.success(userSkillService.get(user.userId(), userSkillId)));
    }

    @Operation(summary = "List the current user's skills")
    @GetMapping
    public ResponseEntity<ApiResponse<List<UserSkillResponseDto>>> list(@CurrentUser AuthenticatedUser user) {
        return ResponseEntity.ok(ApiResponse.success(userSkillService.list(user.userId())));
    }

    @Operation(summary = "Update a user skill assignment")
    @PutMapping("/{userSkillId}")
    public ResponseEntity<ApiResponse<UserSkillResponseDto>> update(
            @CurrentUser AuthenticatedUser user, @PathVariable UUID userSkillId,
            @Valid @RequestBody UserSkillUpdateDto request) {
        return ResponseEntity.ok(ApiResponse.success(
                userSkillService.update(user.userId(), userSkillId, request), "Skill updated"));
    }

    @Operation(summary = "Remove a skill assignment")
    @DeleteMapping("/{userSkillId}")
    public ResponseEntity<ApiResponse<Void>> remove(
            @CurrentUser AuthenticatedUser user, @PathVariable UUID userSkillId) {
        userSkillService.remove(user.userId(), userSkillId);
        return ResponseEntity.ok(ApiResponse.ok("Skill removed"));
    }
}
