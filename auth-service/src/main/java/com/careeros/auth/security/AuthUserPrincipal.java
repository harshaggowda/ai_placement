package com.careeros.auth.security;

import com.careeros.auth.entity.Permission;
import com.careeros.auth.entity.Role;
import com.careeros.auth.entity.User;
import com.careeros.auth.entity.UserStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Spring Security {@link UserDetails} principal for the platform.
 *
 * <p>Carries the user's id alongside the standard details so controllers can resolve the acting user
 * from the security context. Two factories build it: {@link #from(User)} (login path, authorities
 * derived from roles + their permissions while the JPA session is open) and
 * {@link #fromClaims(UUID, String, Collection)} (stateless request path, authorities read from a
 * verified JWT — no database hit).
 */
public class AuthUserPrincipal implements UserDetails {

    private final UUID userId;
    private final String email;
    private final String passwordHash;
    private final boolean active;
    private final Collection<? extends GrantedAuthority> authorities;

    public AuthUserPrincipal(UUID userId, String email, String passwordHash, boolean active,
                             Collection<? extends GrantedAuthority> authorities) {
        this.userId = userId;
        this.email = email;
        this.passwordHash = passwordHash;
        this.active = active;
        this.authorities = authorities;
    }

    /** Build from a managed {@link User}; flattens roles and their permissions into authorities. */
    public static AuthUserPrincipal from(User user) {
        Set<GrantedAuthority> granted = new LinkedHashSet<>();
        for (Role role : user.getRoles()) {
            granted.add(new SimpleGrantedAuthority(role.getName()));
            for (Permission permission : role.getPermissions()) {
                granted.add(new SimpleGrantedAuthority(permission.getName()));
            }
        }
        return new AuthUserPrincipal(user.getId(), user.getEmail(), user.getPasswordHash(),
                user.getStatus() == UserStatus.ACTIVE, granted);
    }

    /** Build from verified JWT claims (no password material, authorities already resolved). */
    public static AuthUserPrincipal fromClaims(UUID userId, String email,
                                               Collection<? extends GrantedAuthority> authorities) {
        return new AuthUserPrincipal(userId, email, null, true, authorities);
    }

    public UUID getUserId() {
        return userId;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    /** The username is the email. */
    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }
}
