package com.careeros.auth.service;

import com.careeros.auth.dto.RegisterRequest;
import com.careeros.auth.dto.UserResponseDto;
import com.careeros.auth.entity.Role;
import com.careeros.auth.entity.User;
import com.careeros.auth.entity.UserStatus;
import com.careeros.auth.mapper.UserMapper;
import com.careeros.auth.repository.RoleRepository;
import com.careeros.auth.repository.UserRepository;
import com.careeros.auth.repository.UserSessionRepository;
import com.careeros.auth.security.JwtService;
import com.careeros.auth.service.impl.AuthServiceImpl;
import com.careeros.auth.service.impl.LoginAttemptAuditor;
import com.careeros.exception.ConflictException;
import com.careeros.security.JwtProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock UserRepository userRepository;
    @Mock RoleRepository roleRepository;
    @Mock UserSessionRepository userSessionRepository;
    @Mock TokenService tokenService;
    @Mock JwtService jwtService;
    @Mock JwtProperties jwtProperties;
    @Mock AuthenticationManager authenticationManager;
    @Mock PasswordEncoder passwordEncoder;
    @Mock UserMapper userMapper;
    @Mock LoginAttemptAuditor loginAuditor;

    @InjectMocks AuthServiceImpl authService;

    @Test
    void registerCreatesUserAssignsDefaultRoleAndIssuesVerificationToken() {
        when(userRepository.existsByEmail("new@careeros.ai")).thenReturn(false);
        Role defaultRole = new Role();
        defaultRole.setName("ROLE_USER");
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(defaultRole));
        when(passwordEncoder.encode("password1")).thenReturn("hashed");

        User saved = new User();
        saved.setId(UUID.randomUUID());
        saved.setEmail("new@careeros.ai");
        when(userRepository.save(any(User.class))).thenReturn(saved);
        when(tokenService.issueEmailVerificationToken(saved)).thenReturn("raw-token");
        UserResponseDto dto = new UserResponseDto(saved.getId(), "new@careeros.ai", "New User",
                UserStatus.PENDING_VERIFICATION, false, null, Set.of("ROLE_USER"), null, null);
        when(userMapper.toResponse(saved)).thenReturn(dto);

        UserResponseDto result = authService.register(
                new RegisterRequest("New@Careeros.ai", "password1", "New User"));

        assertThat(result.email()).isEqualTo("new@careeros.ai");
        assertThat(result.status()).isEqualTo(UserStatus.PENDING_VERIFICATION);
        verify(tokenService).issueEmailVerificationToken(saved);
    }

    @Test
    void registerRejectsDuplicateEmail() {
        when(userRepository.existsByEmail("dupe@careeros.ai")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(
                new RegisterRequest("dupe@careeros.ai", "password1", "Dupe")))
                .isInstanceOf(ConflictException.class);

        verify(userRepository, never()).save(any());
    }
}
