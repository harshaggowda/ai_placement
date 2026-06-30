package com.careeros.auth.controller;

import com.careeros.auth.dto.UserSummaryDto;
import com.careeros.auth.service.UserQueryService;
import com.careeros.common.pagination.PaginationResponse;
import com.careeros.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Administrative user management. Guarded both at the URL ({@code /api/auth/admin/**}) and at the
 * method ({@link PreAuthorize}) — defence in depth demonstrating RBAC + method security.
 */
@RestController
@RequestMapping("/api/auth/admin/users")
@RequiredArgsConstructor
@Tag(name = "Admin - Users", description = "Administrative user queries (ROLE_ADMIN only)")
public class AdminUserController {

    private final UserQueryService userQueryService;

    @Operation(summary = "List users (paginated)")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<ApiResponse<PaginationResponse<UserSummaryDto>>> listUsers(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(userQueryService.listUsers(pageable)));
    }
}
