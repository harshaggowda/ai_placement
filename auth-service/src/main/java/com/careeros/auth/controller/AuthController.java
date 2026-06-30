package com.careeros.auth.controller;

import com.careeros.auth.dto.AuthResponse;
import com.careeros.auth.dto.ForgotPasswordRequest;
import com.careeros.auth.dto.LoginRequest;
import com.careeros.auth.dto.RefreshTokenRequest;
import com.careeros.auth.dto.RegisterRequest;
import com.careeros.auth.dto.ResetPasswordRequest;
import com.careeros.auth.dto.TokenResponse;
import com.careeros.auth.dto.UserResponseDto;
import com.careeros.auth.security.AuthUserPrincipal;
import com.careeros.auth.service.AuthService;
import com.careeros.auth.service.ClientInfo;
import com.careeros.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Authentication and account-lifecycle endpoints. Thin by design: it validates input, derives client
 * metadata, delegates to {@link AuthService}, and wraps results in {@link ApiResponse}. No business
 * logic, no entity exposure.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Registration, login, token, and account-recovery operations")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Register a new account")
    @SecurityRequirements
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponseDto>> register(@Valid @RequestBody RegisterRequest request) {
        UserResponseDto user = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(user, "Registration successful. Please verify your email."));
    }

    @Operation(summary = "Authenticate and obtain access + refresh tokens")
    @SecurityRequirements
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request,
                                                           HttpServletRequest http) {
        AuthResponse response = authService.login(request, clientInfo(http));
        return ResponseEntity.ok(ApiResponse.success(response, "Login successful"));
    }

    @Operation(summary = "Rotate a refresh token for a new access token")
    @SecurityRequirements
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenResponse>> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(ApiResponse.success(authService.refresh(request), "Token refreshed"));
    }

    @Operation(summary = "Verify an email address using a verification token")
    @SecurityRequirements
    @GetMapping("/verify-email")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(@RequestParam @NotBlank String token) {
        authService.verifyEmail(token);
        return ResponseEntity.ok(ApiResponse.ok("Email verified successfully"));
    }

    @Operation(summary = "Begin a password reset")
    @SecurityRequirements
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ResponseEntity.ok(ApiResponse.ok("If the email exists, a reset link has been sent"));
    }

    @Operation(summary = "Complete a password reset")
    @SecurityRequirements
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.ok("Password reset successful"));
    }

    @Operation(summary = "Log out: revoke the refresh token and active sessions")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@Valid @RequestBody RefreshTokenRequest request,
                                                    @AuthenticationPrincipal AuthUserPrincipal principal) {
        authService.logout(request.refreshToken(), principal.getUserId());
        return ResponseEntity.ok(ApiResponse.ok("Logged out"));
    }

    @Operation(summary = "Get the current authenticated user's profile")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponseDto>> me(@AuthenticationPrincipal AuthUserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(authService.getCurrentUser(principal.getUserId())));
    }

    /** Best-effort client metadata for auditing/session tracking (honours a proxy's X-Forwarded-For). */
    private ClientInfo clientInfo(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        String ip = StringUtils.hasText(forwarded) ? forwarded.split(",")[0].trim() : request.getRemoteAddr();
        return new ClientInfo(ip, request.getHeader("User-Agent"));
    }
}
