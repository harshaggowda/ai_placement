package com.careeros.career.mapper;

import com.careeros.career.dto.JobApplicationDto;
import com.careeros.career.dto.JobApplicationResponseDto;
import com.careeros.career.entity.JobApplication;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        uses = {CompanyMapper.class},
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface JobApplicationMapper {

    @Mapping(target = "company", source = "company")
    JobApplicationResponseDto toResponse(JobApplication entity);

    @Mapping(target = "company", ignore = true) // Handled by service
    JobApplication toEntity(JobApplicationDto dto);

    @Mapping(target = "company", ignore = true)
    void updateEntity(JobApplicationDto dto, @MappingTarget JobApplication entity);
}
