package com.careeros.user.service.impl;

import com.careeros.exception.ConflictException;
import com.careeros.exception.ResourceNotFoundException;
import com.careeros.user.dto.UserSkillAssignDto;
import com.careeros.user.dto.UserSkillResponseDto;
import com.careeros.user.dto.UserSkillUpdateDto;
import com.careeros.user.entity.Skill;
import com.careeros.user.entity.SkillCategory;
import com.careeros.user.entity.SkillProficiency;
import com.careeros.user.entity.UserSkill;
import com.careeros.user.event.SkillAssignedEvent;
import com.careeros.user.mapper.UserSkillMapper;
import com.careeros.user.repository.SkillRepository;
import com.careeros.user.repository.UserSkillRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link UserSkillServiceImpl}.
 */
@ExtendWith(MockitoExtension.class)
class UserSkillServiceImplTest {

    @Mock UserSkillRepository userSkillRepository;
    @Mock SkillRepository skillRepository;
    @Mock UserSkillMapper userSkillMapper;
    @Mock ApplicationEventPublisher eventPublisher;

    @InjectMocks UserSkillServiceImpl userSkillService;

    private final UUID userId = UUID.randomUUID();
    private final UUID skillId = UUID.randomUUID();
    private final UUID userSkillId = UUID.randomUUID();

    // -----------------------------------------------------------------------
    // assign()
    // -----------------------------------------------------------------------

    @Test
    void assignHappyPathDefaultsToBeginnerProficiency() {
        when(userSkillRepository.existsByUserIdAndSkillId(userId, skillId)).thenReturn(false);
        Skill skill = makeSkill();
        when(skillRepository.findById(skillId)).thenReturn(Optional.of(skill));
        UserSkill saved = makeUserSkill(skill, SkillProficiency.BEGINNER);
        when(userSkillRepository.save(any(UserSkill.class))).thenReturn(saved);
        when(userSkillMapper.toResponse(saved)).thenReturn(mockResponse());

        // Request with null proficiency
        userSkillService.assign(userId, new UserSkillAssignDto(skillId, null, null, null));

        verify(userSkillRepository).save(argThat(us -> us.getProficiency() == SkillProficiency.BEGINNER));
        verify(eventPublisher).publishEvent(any(SkillAssignedEvent.class));
    }

    @Test
    void assignWithExplicitProficiency() {
        when(userSkillRepository.existsByUserIdAndSkillId(userId, skillId)).thenReturn(false);
        Skill skill = makeSkill();
        when(skillRepository.findById(skillId)).thenReturn(Optional.of(skill));
        UserSkill saved = makeUserSkill(skill, SkillProficiency.EXPERT);
        when(userSkillRepository.save(any(UserSkill.class))).thenReturn(saved);
        when(userSkillMapper.toResponse(saved)).thenReturn(mockResponse());

        userSkillService.assign(userId, new UserSkillAssignDto(skillId, SkillProficiency.EXPERT, null, null));

        verify(userSkillRepository).save(argThat(us -> us.getProficiency() == SkillProficiency.EXPERT));
    }

    @Test
    void assignThrowsConflictWhenAlreadyAssigned() {
        when(userSkillRepository.existsByUserIdAndSkillId(userId, skillId)).thenReturn(true);

        assertThatThrownBy(() -> userSkillService.assign(userId, new UserSkillAssignDto(skillId, null, null, null)))
                .isInstanceOf(ConflictException.class);

        verify(skillRepository, never()).findById(any());
    }

    @Test
    void assignThrowsNotFoundForNonExistentSkill() {
        when(userSkillRepository.existsByUserIdAndSkillId(userId, skillId)).thenReturn(false);
        when(skillRepository.findById(skillId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userSkillService.assign(userId, new UserSkillAssignDto(skillId, null, null, null)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // -----------------------------------------------------------------------
    // remove()
    // -----------------------------------------------------------------------

    @Test
    void removeHappyPath() {
        UserSkill userSkill = makeUserSkill(makeSkill(), SkillProficiency.BEGINNER);
        userSkill.setId(userSkillId);
        userSkill.setUserId(userId);
        when(userSkillRepository.findById(userSkillId)).thenReturn(Optional.of(userSkill));

        userSkillService.remove(userId, userSkillId);

        verify(userSkillRepository).delete(userSkill);
    }

    @Test
    void removeThrowsNotFoundForWrongOwner() {
        UserSkill userSkill = makeUserSkill(makeSkill(), SkillProficiency.BEGINNER);
        userSkill.setId(userSkillId);
        userSkill.setUserId(UUID.randomUUID()); // Different owner
        when(userSkillRepository.findById(userSkillId)).thenReturn(Optional.of(userSkill));

        assertThatThrownBy(() -> userSkillService.remove(userId, userSkillId))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(userSkillRepository, never()).delete(any());
    }

    @Test
    void removeThrowsNotFoundForMissingSkill() {
        when(userSkillRepository.findById(userSkillId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userSkillService.remove(userId, userSkillId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // -----------------------------------------------------------------------
    // update()
    // -----------------------------------------------------------------------

    @Test
    void updateNullFieldsAreIgnored() {
        UserSkill userSkill = makeUserSkill(makeSkill(), SkillProficiency.INTERMEDIATE);
        userSkill.setId(userSkillId);
        userSkill.setUserId(userId);
        when(userSkillRepository.findById(userSkillId)).thenReturn(Optional.of(userSkill));
        when(userSkillMapper.toResponse(userSkill)).thenReturn(mockResponse());

        // Only update proficiency; years stays null
        userSkillService.update(userId, userSkillId,
                new UserSkillUpdateDto(SkillProficiency.EXPERT, null, null, null));

        assertThat(userSkill.getProficiency()).isEqualTo(SkillProficiency.EXPERT);
        assertThat(userSkill.getYearsOfExperience()).isNull();
    }

    @Test
    void updateYearsOfExperience() {
        UserSkill userSkill = makeUserSkill(makeSkill(), SkillProficiency.BEGINNER);
        userSkill.setId(userSkillId);
        userSkill.setUserId(userId);
        when(userSkillRepository.findById(userSkillId)).thenReturn(Optional.of(userSkill));
        when(userSkillMapper.toResponse(userSkill)).thenReturn(mockResponse());

        userSkillService.update(userId, userSkillId,
                new UserSkillUpdateDto(null, BigDecimal.valueOf(2.5), null, null));

        assertThat(userSkill.getYearsOfExperience()).isEqualByComparingTo(BigDecimal.valueOf(2.5));
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private Skill makeSkill() {
        Skill s = new Skill();
        s.setId(skillId);
        s.setName("Java");
        s.setCategory(SkillCategory.LANGUAGE);
        return s;
    }

    private UserSkill makeUserSkill(Skill skill, SkillProficiency proficiency) {
        UserSkill us = new UserSkill();
        us.setUserId(userId);
        us.setSkill(skill);
        us.setProficiency(proficiency);
        return us;
    }

    private static UserSkillResponseDto mockResponse() {
        return new UserSkillResponseDto(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                "Java", null, SkillCategory.LANGUAGE, SkillProficiency.BEGINNER,
                null, null, false, Instant.now());
    }
}
