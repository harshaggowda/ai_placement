package com.careeros.user.controller;

import com.careeros.common.pagination.PaginationResponse;
import com.careeros.common.response.ApiResponse;
import com.careeros.security.AuthenticatedUser;
import com.careeros.security.CurrentUser;
import com.careeros.user.dto.SkillCreateDto;
import com.careeros.user.dto.SkillResponseDto;
import com.careeros.user.dto.SkillUpdateDto;
import com.careeros.user.entity.SkillCategory;
import com.careeros.user.service.SkillService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users/skills")
@RequiredArgsConstructor
@Tag(name = "Skills", description = "Platform skill catalog (admin-facing)")
public class SkillController {

    private final SkillService skillService;

    @Operation(summary = "Create a skill (admin)")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<SkillResponseDto>> create(@Valid @RequestBody SkillCreateDto request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(skillService.create(request), "Skill created"));
    }

    @Operation(summary = "Get a skill by id")
    @GetMapping("/{skillId}")
    public ResponseEntity<ApiResponse<SkillResponseDto>> get(@PathVariable UUID skillId) {
        return ResponseEntity.ok(ApiResponse.success(skillService.get(skillId)));
    }

    @Operation(summary = "List all skills")
    @GetMapping
    public ResponseEntity<ApiResponse<PaginationResponse<SkillResponseDto>>> list(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(PaginationResponse.from(skillService.list(pageable))));
    }

    @Operation(summary = "Search skills")
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PaginationResponse<SkillResponseDto>>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) SkillCategory category,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(
                PaginationResponse.from(skillService.search(keyword, category, pageable))));
    }

    @Operation(summary = "Update a skill (admin)")
    @PutMapping("/{skillId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<SkillResponseDto>> update(
            @PathVariable UUID skillId, @Valid @RequestBody SkillUpdateDto request) {
        return ResponseEntity.ok(ApiResponse.success(skillService.update(skillId, request), "Skill updated"));
    }

    @Operation(summary = "Soft-delete a skill (admin)")
    @DeleteMapping("/{skillId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID skillId) {
        skillService.delete(skillId);
        return ResponseEntity.ok(ApiResponse.ok("Skill deleted"));
    }
}
