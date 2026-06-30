package com.careeros.user.mapper;

import com.careeros.user.dto.RoadmapTaskCreateDto;
import com.careeros.user.dto.RoadmapTaskResponseDto;
import com.careeros.user.dto.RoadmapTaskUpdateDto;
import com.careeros.user.entity.RoadmapTask;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface RoadmapTaskMapper {

    @Mapping(target = "roadmapId", source = "roadmap.id")
    RoadmapTaskResponseDto toResponse(RoadmapTask task);

    @Mapping(target = "roadmap", ignore = true)
    RoadmapTask toEntity(RoadmapTaskCreateDto dto);

    @Mapping(target = "roadmap", ignore = true)
    void updateEntity(RoadmapTaskUpdateDto dto, @MappingTarget RoadmapTask task);
}
