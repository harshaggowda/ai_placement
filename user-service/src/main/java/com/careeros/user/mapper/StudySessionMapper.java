package com.careeros.user.mapper;

import com.careeros.user.dto.StudySessionResponseDto;
import com.careeros.user.entity.StudySession;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface StudySessionMapper {
    StudySessionResponseDto toResponse(StudySession session);
}
