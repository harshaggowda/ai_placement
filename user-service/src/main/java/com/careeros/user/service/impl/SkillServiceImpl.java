package com.careeros.user.service.impl;

import com.careeros.exception.ConflictException;
import com.careeros.exception.ResourceNotFoundException;
import com.careeros.user.dto.SkillCreateDto;
import com.careeros.user.dto.SkillResponseDto;
import com.careeros.user.dto.SkillUpdateDto;
import com.careeros.user.entity.Skill;
import com.careeros.user.entity.SkillCategory;
import com.careeros.user.mapper.SkillMapper;
import com.careeros.user.repository.SkillRepository;
import com.careeros.user.service.SkillService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class SkillServiceImpl implements SkillService {

    private final SkillRepository skillRepository;
    private final SkillMapper skillMapper;

    @Override
    public SkillResponseDto create(SkillCreateDto request) {
        if (skillRepository.existsByNameIgnoreCase(request.name())) {
            throw new ConflictException("Skill", "name", request.name());
        }
        Skill skill = skillMapper.toEntity(request);
        Skill saved = skillRepository.save(skill);
        log.info("Skill created: id={} name='{}'", saved.getId(), saved.getName());
        return skillMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public SkillResponseDto get(UUID skillId) {
        return skillMapper.toResponse(requireSkill(skillId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SkillResponseDto> list(Pageable pageable) {
        return skillRepository.findAll(pageable).map(skillMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SkillResponseDto> search(String keyword, SkillCategory category, Pageable pageable) {
        if (keyword != null && category != null) {
            return skillRepository.findByCategoryAndNameContainingIgnoreCase(category, keyword, pageable)
                    .map(skillMapper::toResponse);
        } else if (keyword != null) {
            return skillRepository.findByNameContainingIgnoreCase(keyword, pageable)
                    .map(skillMapper::toResponse);
        } else if (category != null) {
            return skillRepository.findByCategory(category, pageable)
                    .map(skillMapper::toResponse);
        } else {
            return list(pageable);
        }
    }

    @Override
    public SkillResponseDto update(UUID skillId, SkillUpdateDto request) {
        Skill skill = requireSkill(skillId);
        skillMapper.updateEntity(request, skill);
        log.info("Skill updated: id={} name='{}'", skillId, skill.getName());
        return skillMapper.toResponse(skill);
    }

    @Override
    public void delete(UUID skillId) {
        skillRepository.delete(requireSkill(skillId));
        log.info("Skill soft-deleted: id={}", skillId);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<SkillCategory, Long> countByCategory() {
        // Single GROUP BY query — avoids one SELECT per enum value.
        Map<SkillCategory, Long> result = new EnumMap<>(SkillCategory.class);
        // Seed all categories with zero so callers always get a complete map.
        Arrays.stream(SkillCategory.values()).forEach(c -> result.put(c, 0L));
        skillRepository.countGroupedByCategory()
                .forEach(row -> result.put((SkillCategory) row[0], (Long) row[1]));
        return result;
    }

    private Skill requireSkill(UUID skillId) {
        return skillRepository.findById(skillId)
                .orElseThrow(() -> new ResourceNotFoundException("Skill", "id", skillId));
    }
}
