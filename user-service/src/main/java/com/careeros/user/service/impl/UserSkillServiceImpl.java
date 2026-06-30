package com.careeros.user.service.impl;

import com.careeros.exception.ConflictException;
import com.careeros.exception.ResourceNotFoundException;
import com.careeros.user.dto.UserSkillAssignDto;
import com.careeros.user.dto.UserSkillResponseDto;
import com.careeros.user.dto.UserSkillUpdateDto;
import com.careeros.user.entity.Skill;
import com.careeros.user.entity.SkillProficiency;
import com.careeros.user.entity.UserSkill;
import com.careeros.user.event.SkillAssignedEvent;
import com.careeros.user.mapper.UserSkillMapper;
import com.careeros.user.repository.SkillRepository;
import com.careeros.user.repository.UserSkillRepository;
import com.careeros.user.service.UserSkillService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UserSkillServiceImpl implements UserSkillService {

    private final UserSkillRepository userSkillRepository;
    private final SkillRepository skillRepository;
    private final UserSkillMapper userSkillMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public UserSkillResponseDto assign(UUID userId, UserSkillAssignDto request) {
        if (userSkillRepository.existsByUserIdAndSkillId(userId, request.skillId())) {
            throw new ConflictException("UserSkill", "skillId", request.skillId());
        }
        Skill skill = skillRepository.findById(request.skillId())
                .orElseThrow(() -> new ResourceNotFoundException("Skill", "id", request.skillId()));

        UserSkill userSkill = new UserSkill();
        userSkill.setUserId(userId);
        userSkill.setSkill(skill);
        userSkill.setProficiency(request.proficiency() != null ? request.proficiency() : SkillProficiency.BEGINNER);
        userSkill.setYearsOfExperience(request.yearsOfExperience());
        userSkill.setLastPracticedDate(request.lastPracticedDate());

        UserSkill saved = userSkillRepository.save(userSkill);
        eventPublisher.publishEvent(new SkillAssignedEvent(userId, skill.getId(), skill.getName()));
        log.info("UserSkill assigned: userId={} skillId={} skillName='{}'", userId, skill.getId(), skill.getName());
        return userSkillMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public UserSkillResponseDto get(UUID userId, UUID userSkillId) {
        return userSkillMapper.toResponse(requireOwnedUserSkill(userId, userSkillId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserSkillResponseDto> list(UUID userId) {
        return userSkillRepository.findAllByUserId(userId).stream()
                .map(userSkillMapper::toResponse)
                .toList();
    }

    @Override
    public UserSkillResponseDto update(UUID userId, UUID userSkillId, UserSkillUpdateDto request) {
        UserSkill userSkill = requireOwnedUserSkill(userId, userSkillId);
        if (request.proficiency() != null) {
            userSkill.setProficiency(request.proficiency());
        }
        if (request.yearsOfExperience() != null) {
            userSkill.setYearsOfExperience(request.yearsOfExperience());
        }
        if (request.lastPracticedDate() != null) {
            userSkill.setLastPracticedDate(request.lastPracticedDate());
        }
        if (request.isVerified() != null) {
            userSkill.setVerified(request.isVerified());
        }
        log.info("UserSkill updated: userId={} userSkillId={}", userId, userSkillId);
        return userSkillMapper.toResponse(userSkill);
    }

    @Override
    public void remove(UUID userId, UUID userSkillId) {
        UserSkill userSkill = requireOwnedUserSkill(userId, userSkillId);
        userSkillRepository.delete(userSkill);
        log.info("UserSkill removed: userId={} userSkillId={}", userId, userSkillId);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<SkillProficiency, Long> countByProficiency(UUID userId) {
        List<Object[]> raw = userSkillRepository.countByProficiencyForUser(userId);
        return raw.stream().collect(Collectors.toMap(
                arr -> (SkillProficiency) arr[0],
                arr -> (Long) arr[1]));
    }

    private UserSkill requireOwnedUserSkill(UUID userId, UUID userSkillId) {
        UserSkill userSkill = userSkillRepository.findById(userSkillId)
                .orElseThrow(() -> new ResourceNotFoundException("UserSkill", "id", userSkillId));
        if (!userSkill.getUserId().equals(userId)) {
            throw new ResourceNotFoundException("UserSkill", "id", userSkillId);
        }
        return userSkill;
    }
}
