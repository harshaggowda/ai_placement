package com.careeros.career.mapper;

import com.careeros.career.dto.ResumeAnalysisMetadataDto;
import com.careeros.career.dto.ResumeAnalysisMetadataResponseDto;
import com.careeros.career.entity.ResumeAnalysisMetadata;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ResumeAnalysisMetadataMapper {

    ResumeAnalysisMetadataResponseDto toResponse(ResumeAnalysisMetadata entity);

    ResumeAnalysisMetadata toEntity(ResumeAnalysisMetadataDto dto);

    void updateEntity(ResumeAnalysisMetadataDto dto, @MappingTarget ResumeAnalysisMetadata entity);
}
