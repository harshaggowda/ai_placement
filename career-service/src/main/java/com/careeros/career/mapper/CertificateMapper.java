package com.careeros.career.mapper;

import com.careeros.career.dto.CertificateDto;
import com.careeros.career.dto.CertificateResponseDto;
import com.careeros.career.entity.Certificate;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CertificateMapper {

    CertificateResponseDto toResponse(Certificate entity);

    Certificate toEntity(CertificateDto dto);

    void updateEntity(CertificateDto dto, @MappingTarget Certificate entity);
}
