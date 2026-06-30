package com.careeros.user.mapper;

import com.careeros.user.dto.SkillCreateDto;
import com.careeros.user.dto.SkillResponseDto;
import com.careeros.user.dto.SkillUpdateDto;
import com.careeros.user.entity.Skill;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface SkillMapper {
    SkillResponseDto toResponse(Skill skill);
    Skill toEntity(SkillCreateDto dto);
    void updateEntity(SkillUpdateDto dto, @MappingTarget Skill skill);
}
