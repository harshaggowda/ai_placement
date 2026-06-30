package com.careeros.user.mapper;

import com.careeros.user.dto.ProfileCreateDto;
import com.careeros.user.dto.ProfileResponseDto;
import com.careeros.user.dto.ProfileUpdateDto;
import com.careeros.user.entity.UserProfile;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * MapStruct mapper for {@link UserProfile}. {@code nullValueCheckStrategy = ALWAYS} means null inputs
 * never overwrite existing values (true partial updates) nor clobber entity defaults on create.
 */
@Mapper(componentModel = "spring",
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserProfileMapper {

    @Mapping(target = "completionPercentage", source = "completion")
    ProfileResponseDto toResponse(UserProfile profile, int completion);

    UserProfile toEntity(ProfileCreateDto dto);

    void updateEntity(ProfileUpdateDto dto, @MappingTarget UserProfile profile);
}
