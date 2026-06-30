package com.careeros.user.mapper;

import com.careeros.user.dto.GoalCreateDto;
import com.careeros.user.dto.GoalResponseDto;
import com.careeros.user.dto.GoalSummaryDto;
import com.careeros.user.dto.GoalUpdateDto;
import com.careeros.user.entity.Goal;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * MapStruct mapper for {@link Goal}. Null-safe so partial updates and optional create fields never
 * overwrite values or entity defaults.
 */
@Mapper(componentModel = "spring",
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface GoalMapper {

    GoalResponseDto toResponse(Goal goal);

    GoalSummaryDto toSummary(Goal goal);

    Goal toEntity(GoalCreateDto dto);

    void updateEntity(GoalUpdateDto dto, @MappingTarget Goal goal);
}
