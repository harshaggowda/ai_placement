package com.careeros.auth.mapper;

import com.careeros.auth.dto.UserResponseDto;
import com.careeros.auth.dto.UserSummaryDto;
import com.careeros.auth.entity.Role;
import com.careeros.auth.entity.User;
import org.mapstruct.Mapper;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * MapStruct mapper: {@link User} entity → outbound DTOs. Never exposes the entity or password hash.
 */
@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponseDto toResponse(User user);

    UserSummaryDto toSummary(User user);

    /** Flattens the role association to a set of role names for the response payload. */
    default Set<String> mapRoles(Set<Role> roles) {
        return roles == null ? Set.of()
                : roles.stream().map(Role::getName).collect(Collectors.toUnmodifiableSet());
    }
}
