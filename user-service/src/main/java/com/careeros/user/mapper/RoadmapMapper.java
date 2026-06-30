package com.careeros.user.mapper;

import com.careeros.user.dto.RoadmapCreateDto;
import com.careeros.user.dto.RoadmapResponseDto;
import com.careeros.user.dto.RoadmapUpdateDto;
import com.careeros.user.entity.Roadmap;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface RoadmapMapper {

    @Mapping(target = "totalTasks",      source = "totalTasks")
    @Mapping(target = "completedTasks",  source = "completedTasks")
    @Mapping(target = "progressPercent", source = "progressPercent")
    RoadmapResponseDto toResponse(Roadmap roadmap, int totalTasks, int completedTasks, int progressPercent);

    Roadmap toEntity(RoadmapCreateDto dto);

    void updateEntity(RoadmapUpdateDto dto, @MappingTarget Roadmap roadmap);
}
