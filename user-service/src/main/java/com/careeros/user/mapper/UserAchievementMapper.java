package com.careeros.user.mapper;

import com.careeros.user.dto.UserAchievementResponseDto;
import com.careeros.user.entity.UserAchievement;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserAchievementMapper {

    @Mapping(target = "achievementId",  source = "achievement.id")
    @Mapping(target = "code",           source = "achievement.code")
    @Mapping(target = "title",          source = "achievement.title")
    @Mapping(target = "description",    source = "achievement.description")
    @Mapping(target = "category",       source = "achievement.category")
    @Mapping(target = "iconUrl",        source = "achievement.iconUrl")
    UserAchievementResponseDto toResponse(UserAchievement ua);
}
