package com.careeros.auth.security;

import com.careeros.auth.entity.User;
import com.careeros.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Loads a user (by email) for Spring Security's {@code DaoAuthenticationProvider}.
 *
 * <p>Runs read-only and transactional so the user's roles and permissions are initialized while the
 * persistence context is open, producing a fully-populated {@link AuthUserPrincipal}.
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("No account for email: " + email));
        return AuthUserPrincipal.from(user);
    }
}
