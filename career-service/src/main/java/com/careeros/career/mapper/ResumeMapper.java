package com.careeros.career.mapper;

import com.careeros.career.dto.ResumeDto;
import com.careeros.career.dto.ResumeResponseDto;
import com.careeros.career.entity.Resume;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ResumeMapper {

    ResumeResponseDto toResponse(Resume entity);

    Resume toEntity(ResumeDto dto);

    void updateEntity(ResumeDto dto, @MappingTarget Resume entity);
}
