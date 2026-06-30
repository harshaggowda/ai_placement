package com.careeros.user.service;

import com.careeros.user.dto.SkillCreateDto;
import com.careeros.user.dto.SkillResponseDto;
import com.careeros.user.dto.SkillUpdateDto;
import com.careeros.user.entity.SkillCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;
import java.util.UUID;

/** Platform-wide skill catalog management (admin-facing). */
public interface SkillService {

    SkillResponseDto create(SkillCreateDto request);

    SkillResponseDto get(UUID skillId);

    Page<SkillResponseDto> list(Pageable pageable);

    Page<SkillResponseDto> search(String keyword, SkillCategory category, Pageable pageable);

    SkillResponseDto update(UUID skillId, SkillUpdateDto request);

    void delete(UUID skillId);

    Map<SkillCategory, Long> countByCategory();
}
