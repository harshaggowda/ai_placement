package com.careeros.auth.service.impl;

import com.careeros.auth.dto.AuthResponse;
import com.careeros.auth.dto.ForgotPasswordRequest;
import com.careeros.auth.dto.LoginRequest;
import com.careeros.auth.dto.RefreshTokenRequest;
import com.careeros.auth.dto.RegisterRequest;
import com.careeros.auth.dto.ResetPasswordRequest;
import com.careeros.auth.dto.TokenResponse;
import com.careeros.auth.dto.UserResponseDto;
import com.careeros.auth.entity.EmailVerificationToken;
import com.careeros.auth.entity.PasswordResetToken;
import com.careeros.auth.entity.RefreshToken;
import com.careeros.auth.entity.Role;
import com.careeros.auth.entity.User;
import com.careeros.auth.entity.UserSession;
import com.careeros.auth.entity.UserStatus;
import com.careeros.auth.mapper.UserMapper;
import com.careeros.auth.repository.RoleRepository;
import com.careeros.auth.repository.UserRepository;
import com.careeros.auth.repository.UserSessionRepository;
import com.careeros.auth.security.AuthUserPrincipal;
import com.careeros.auth.security.JwtService;
import com.careeros.auth.service.AuthService;
import com.careeros.auth.service.ClientInfo;
import com.careeros.auth.service.TokenService;
import com.careeros.exception.ConflictException;
import com.careeros.exception.InternalServerException;
import com.careeros.exception.ResourceNotFoundException;
import com.careeros.exception.UnauthorizedException;
import com.careeros.security.JwtProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

/**
 * Default {@link AuthService}: registration, login, token refresh/rotation, logout, email
 * verification, and the forgot/reset-password flows.
 *
 * <p>Class-level {@code @Transactional} makes each use case atomic; read paths override to read-only.
 * Failed-login auditing is delegated to {@link LoginAttemptAuditor} (new transaction) so it survives
 * the rollback triggered by the thrown {@link UnauthorizedException}. No web types leak in — client
 * metadata arrives as {@link ClientInfo}.
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final String DEFAULT_ROLE = "ROLE_USER";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserSessionRepository userSessionRepository;
    private final TokenService tokenService;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final LoginAttemptAuditor loginAuditor;

    @Override
    public UserResponseDto register(RegisterRequest request) {
        String email = normalizeEmail(request.email());
        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("User", "email", email);
        }
        Role defaultRole = roleRepository.findByName(DEFAULT_ROLE)
                .orElseThrow(() -> new InternalServerException("Default role not configured: " + DEFAULT_ROLE));

        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setDisplayName(request.displayName());
        user.setStatus(UserStatus.PENDING_VERIFICATION);
        user.setEmailVerified(false);
        user.getRoles().add(defaultRole);
        User saved = userRepository.save(user);

        tokenService.issueEmailVerificationToken(saved);
        log.info("User registered: userId={}, email={} (verification email dispatched by notification service)",
                saved.getId(), email);
        return userMapper.toResponse(saved);
    }

    @Override
    public AuthResponse login(LoginRequest request, ClientInfo client) {
        String email = normalizeEmail(request.email());
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, request.password()));
            AuthUserPrincipal principal = (AuthUserPrincipal) authentication.getPrincipal();

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

            String accessToken = jwtService.generateAccessToken(principal);
            String refreshToken = tokenService.issueRefreshToken(user);
            user.setLastLoginAt(Instant.now());
            openSession(user, client);
            loginAuditor.record(email, client, true, null);

            log.info("Login success: userId={}", user.getId());
            return AuthResponse.of(accessToken, refreshToken, jwtService.accessTokenTtlSeconds(),
                    userMapper.toResponse(user));
        } catch (AuthenticationException ex) {
            loginAuditor.record(email, client, false, ex.getClass().getSimpleName());
            log.warn("Login failed for email={}: {}", email, ex.getMessage());
            throw new UnauthorizedException("Invalid email or password");
        }
    }

    @Override
    public TokenResponse refresh(RefreshTokenRequest request) {
        RefreshToken current = tokenService.validateRefreshToken(request.refreshToken());
        User user = current.getUser();
        AuthUserPrincipal principal = AuthUserPrincipal.from(user);

        String accessToken = jwtService.generateAccessToken(principal);
        String newRefreshToken = tokenService.rotateRefreshToken(current, user);

        log.info("Access token refreshed: userId={}", user.getId());
        return TokenResponse.of(accessToken, newRefreshToken, jwtService.accessTokenTtlSeconds());
    }

    @Override
    public void logout(String refreshToken, UUID userId) {
        tokenService.revokeRefreshToken(refreshToken);
        Instant now = Instant.now();
        userSessionRepository.findAllByUserIdAndRevokedAtIsNull(userId)
                .forEach(session -> session.setRevokedAt(now));
        log.info("Logout: userId={}", userId);
    }

    @Override
    public void verifyEmail(String token) {
        EmailVerificationToken verification = tokenService.consumeEmailVerificationToken(token);
        User user = verification.getUser();
        user.setEmailVerified(true);
        if (user.getStatus() == UserStatus.PENDING_VERIFICATION) {
            user.setStatus(UserStatus.ACTIVE);
        }
        verification.setVerifiedAt(Instant.now());
        log.info("Email verified, account activated: userId={}", user.getId());
    }

    @Override
    public void forgotPassword(ForgotPasswordRequest request) {
        String email = normalizeEmail(request.email());
        userRepository.findByEmail(email).ifPresentOrElse(
                user -> {
                    tokenService.issuePasswordResetToken(user);
                    log.info("Password reset requested: userId={} (reset email dispatched by notification service)",
                            user.getId());
                },
                () -> log.info("Password reset requested for unknown email; ignored to avoid account enumeration"));
    }

    @Override
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken resetToken = tokenService.consumePasswordResetToken(request.token());
        User user = resetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        resetToken.setUsedAt(Instant.now());
        tokenService.revokeAllRefreshTokens(user.getId());
        log.info("Password reset completed; refresh tokens revoked: userId={}", user.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDto getCurrentUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        return userMapper.toResponse(user);
    }

    private void openSession(User user, ClientInfo client) {
        UserSession session = new UserSession();
        session.setUser(user);
        session.setIpAddress(client.ipAddress());
        session.setUserAgent(client.userAgent());
        session.setLastSeenAt(Instant.now());
        session.setExpiresAt(Instant.now().plus(jwtProperties.getRefreshTokenExpiration()));
        userSessionRepository.save(session);
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }
}
