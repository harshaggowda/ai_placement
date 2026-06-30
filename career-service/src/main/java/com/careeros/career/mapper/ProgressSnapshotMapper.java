package com.careeros.career.mapper;

import com.careeros.career.dto.ProgressSnapshotDto;
import com.careeros.career.dto.ProgressSnapshotResponseDto;
import com.careeros.career.entity.ProgressSnapshot;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ProgressSnapshotMapper {

    ProgressSnapshotResponseDto toResponse(ProgressSnapshot entity);

    ProgressSnapshot toEntity(ProgressSnapshotDto dto);

    void updateEntity(ProgressSnapshotDto dto, @MappingTarget ProgressSnapshot entity);
}
