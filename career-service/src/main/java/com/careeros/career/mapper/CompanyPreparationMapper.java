package com.careeros.career.mapper;

import com.careeros.career.dto.CompanyPreparationDto;
import com.careeros.career.dto.CompanyPreparationResponseDto;
import com.careeros.career.entity.CompanyPreparation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        uses = {CompanyMapper.class},
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CompanyPreparationMapper {

    @Mapping(target = "company", source = "company")
    CompanyPreparationResponseDto toResponse(CompanyPreparation entity);

    @Mapping(target = "company", ignore = true) // Handled by service
    CompanyPreparation toEntity(CompanyPreparationDto dto);

    @Mapping(target = "company", ignore = true)
    void updateEntity(CompanyPreparationDto dto, @MappingTarget CompanyPreparation entity);
}
