package com.careeros.user.service;

import com.careeros.user.dto.UserSkillAssignDto;
import com.careeros.user.dto.UserSkillResponseDto;
import com.careeros.user.dto.UserSkillUpdateDto;
import com.careeros.user.entity.SkillProficiency;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/** User-scoped skill assignments. */
public interface UserSkillService {

    UserSkillResponseDto assign(UUID userId, UserSkillAssignDto request);

    UserSkillResponseDto get(UUID userId, UUID userSkillId);

    List<UserSkillResponseDto> list(UUID userId);

    UserSkillResponseDto update(UUID userId, UUID userSkillId, UserSkillUpdateDto request);

    void remove(UUID userId, UUID userSkillId);

    Map<SkillProficiency, Long> countByProficiency(UUID userId);
}
