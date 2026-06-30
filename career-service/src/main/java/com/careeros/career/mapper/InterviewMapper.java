package com.careeros.career.mapper;

import com.careeros.career.dto.InterviewDto;
import com.careeros.career.dto.InterviewResponseDto;
import com.careeros.career.entity.Interview;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface InterviewMapper {

    @Mapping(target = "applicationId", source = "application.id")
    InterviewResponseDto toResponse(Interview entity);

    @Mapping(target = "application", ignore = true) // Handled by service
    Interview toEntity(InterviewDto dto);

    @Mapping(target = "application", ignore = true)
    void updateEntity(InterviewDto dto, @MappingTarget Interview entity);
}
