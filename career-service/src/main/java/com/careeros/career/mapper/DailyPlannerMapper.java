package com.careeros.career.mapper;

import com.careeros.career.dto.DailyPlannerDto;
import com.careeros.career.dto.DailyPlannerResponseDto;
import com.careeros.career.entity.DailyPlanner;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface DailyPlannerMapper {

    DailyPlannerResponseDto toResponse(DailyPlanner entity);

    DailyPlanner toEntity(DailyPlannerDto dto);

    void updateEntity(DailyPlannerDto dto, @MappingTarget DailyPlanner entity);
}
