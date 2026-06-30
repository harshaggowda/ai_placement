package com.careeros.career.mapper;

import com.careeros.career.dto.ProjectDto;
import com.careeros.career.dto.ProjectResponseDto;
import com.careeros.career.entity.Project;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ProjectMapper {

    ProjectResponseDto toResponse(Project entity);

    Project toEntity(ProjectDto dto);

    void updateEntity(ProjectDto dto, @MappingTarget Project entity);
}
