package com.careeros.user.mapper;

import com.careeros.user.dto.UserSkillResponseDto;
import com.careeros.user.entity.UserSkill;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserSkillMapper {

    @Mapping(target = "skillId",          source = "skill.id")
    @Mapping(target = "skillName",        source = "skill.name")
    @Mapping(target = "skillDescription", source = "skill.description")
    @Mapping(target = "skillCategory",    source = "skill.category")
    @Mapping(target = "isVerified",       source = "verified")
    UserSkillResponseDto toResponse(UserSkill userSkill);
}
