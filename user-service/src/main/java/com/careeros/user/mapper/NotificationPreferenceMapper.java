package com.careeros.user.mapper;

import com.careeros.user.dto.NotificationPreferenceDto;
import com.careeros.user.dto.NotificationPreferenceResponseDto;
import com.careeros.user.entity.NotificationPreference;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface NotificationPreferenceMapper {
    NotificationPreferenceResponseDto toResponse(NotificationPreference pref);
    void updateEntity(NotificationPreferenceDto dto, @MappingTarget NotificationPreference pref);
}
