package com.careeros.user.mapper;

import com.careeros.user.dto.DashboardSettingsDto;
import com.careeros.user.dto.DashboardSettingsResponseDto;
import com.careeros.user.entity.DashboardSettings;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface DashboardSettingsMapper {
    DashboardSettingsResponseDto toResponse(DashboardSettings settings);
    void updateEntity(DashboardSettingsDto dto, @MappingTarget DashboardSettings settings);
}
