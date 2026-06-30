package com.careeros.career.mapper;

import com.careeros.career.dto.CompanyDto;
import com.careeros.career.dto.CompanyResponseDto;
import com.careeros.career.entity.Company;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CompanyMapper {

    CompanyResponseDto toResponse(Company entity);

    Company toEntity(CompanyDto dto);

    void updateEntity(CompanyDto dto, @MappingTarget Company entity);
}
