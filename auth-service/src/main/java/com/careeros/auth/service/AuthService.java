package com.careeros.auth.service;

import com.careeros.auth.dto.AuthResponse;
import com.careeros.auth.dto.ForgotPasswordRequest;
import com.careeros.auth.dto.LoginRequest;
import com.careeros.auth.dto.RefreshTokenRequest;
import com.careeros.auth.dto.RegisterRequest;
import com.careeros.auth.dto.ResetPasswordRequest;
import com.careeros.auth.dto.TokenResponse;
import com.careeros.auth.dto.UserResponseDto;

import java.util.UUID;

/**
 * Authentication and account-lifecycle use cases. Implementations own all business rules,
 * transactions, and auditing; controllers stay thin.
 */
public interface AuthService {

    /** Register a new account (default role, pending email verification). Returns the created user. */
    UserResponseDto register(RegisterRequest request);

    /** Authenticate credentials and issue access + refresh tokens. */
    AuthResponse login(LoginRequest request, ClientInfo client);

    /** Rotate a valid refresh token and mint a new access token. */
    TokenResponse refresh(RefreshTokenRequest request);

    /** Revoke the given refresh token and the caller's active sessions. */
    void logout(String refreshToken, UUID userId);

    /** Activate an account from a valid email-verification token. */
    void verifyEmail(String token);

    /** Begin a password reset (no-op response if the email is unknown — never leaks existence). */
    void forgotPassword(ForgotPasswordRequest request);

    /** Complete a password reset, then expire all of the user's refresh tokens. */
    void resetPassword(ResetPasswordRequest request);

    /** Current user's profile, by id (from the authenticated principal). */
    UserResponseDto getCurrentUser(UUID userId);
}
