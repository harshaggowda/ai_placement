package com.careeros.user.service.impl;

import com.careeros.exception.ConflictException;
import com.careeros.exception.ResourceNotFoundException;
import com.careeros.user.dto.SkillCreateDto;
import com.careeros.user.dto.SkillResponseDto;
import com.careeros.user.entity.Skill;
import com.careeros.user.entity.SkillCategory;
import com.careeros.user.mapper.SkillMapper;
import com.careeros.user.repository.SkillRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link SkillServiceImpl}, including the N+1 fix validation.
 */
@ExtendWith(MockitoExtension.class)
class SkillServiceImplTest {

    @Mock SkillRepository skillRepository;
    @Mock SkillMapper skillMapper;

    @InjectMocks SkillServiceImpl skillService;

    // -----------------------------------------------------------------------
    // create()
    // -----------------------------------------------------------------------

    @Test
    void createSkillSuccessfully() {
        when(skillRepository.existsByNameIgnoreCase("Java")).thenReturn(false);
        Skill skill = makeSkill("Java");
        when(skillMapper.toEntity(any(SkillCreateDto.class))).thenReturn(skill);
        when(skillRepository.save(skill)).thenReturn(skill);
        when(skillMapper.toResponse(skill)).thenReturn(mockResponse());

        skillService.create(new SkillCreateDto("Java", null, SkillCategory.LANGUAGE, null));

        verify(skillRepository).save(skill);
    }

    @Test
    void createThrowsConflictOnDuplicateName() {
        when(skillRepository.existsByNameIgnoreCase("java")).thenReturn(true);

        assertThatThrownBy(() ->
                skillService.create(new SkillCreateDto("java", null, SkillCategory.LANGUAGE, null)))
                .isInstanceOf(ConflictException.class);

        verify(skillRepository, never()).save(any());
    }

    // -----------------------------------------------------------------------
    // get()
    // -----------------------------------------------------------------------

    @Test
    void getThrowsNotFoundForMissingSkill() {
        UUID skillId = UUID.randomUUID();
        when(skillRepository.findById(skillId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> skillService.get(skillId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // -----------------------------------------------------------------------
    // search()
    // -----------------------------------------------------------------------

    @Test
    void searchWithKeywordAndCategoryUsesCorrectRepoMethod() {
        when(skillRepository.findByCategoryAndNameContainingIgnoreCase(
                eq(SkillCategory.LANGUAGE), eq("java"), any()))
                .thenReturn(new PageImpl<>(List.of()));

        skillService.search("java", SkillCategory.LANGUAGE, PageRequest.of(0, 10));

        verify(skillRepository).findByCategoryAndNameContainingIgnoreCase(
                SkillCategory.LANGUAGE, "java", PageRequest.of(0, 10));
        verify(skillRepository, never()).findByNameContainingIgnoreCase(any(), any());
        verify(skillRepository, never()).findByCategory(any(), any());
    }

    @Test
    void searchWithKeywordOnlyUsesNameContaining() {
        when(skillRepository.findByNameContainingIgnoreCase(eq("kotlin"), any()))
                .thenReturn(new PageImpl<>(List.of()));

        skillService.search("kotlin", null, PageRequest.of(0, 10));

        verify(skillRepository).findByNameContainingIgnoreCase("kotlin", PageRequest.of(0, 10));
        verify(skillRepository, never()).findByCategory(any(), any());
    }

    @Test
    void searchWithCategoryOnlyUsesCategoryFilter() {
        when(skillRepository.findByCategory(eq(SkillCategory.FRAMEWORK), any()))
                .thenReturn(new PageImpl<>(List.of()));

        skillService.search(null, SkillCategory.FRAMEWORK, PageRequest.of(0, 10));

        verify(skillRepository).findByCategory(SkillCategory.FRAMEWORK, PageRequest.of(0, 10));
        verify(skillRepository, never()).findByNameContainingIgnoreCase(any(), any());
    }

    @Test
    void searchWithNoFiltersListsAll() {
        when(skillRepository.findAll(any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        skillService.search(null, null, PageRequest.of(0, 10));

        verify(skillRepository).findAll(PageRequest.of(0, 10));
    }

    // -----------------------------------------------------------------------
    // countByCategory() — N+1 fix validation
    // -----------------------------------------------------------------------

    @Test
    void countByCategoryUsesGroupByQueryNotNPerCategory() {
        // Stub the GROUP BY query (Bug Fix #3)
        when(skillRepository.countGroupedByCategory()).thenReturn(List.of(
                new Object[]{SkillCategory.LANGUAGE, 5L},
                new Object[]{SkillCategory.FRAMEWORK, 3L}
        ));

        Map<SkillCategory, Long> result = skillService.countByCategory();

        // Should have called the GROUP BY query exactly once
        verify(skillRepository, times(1)).countGroupedByCategory();
        // Should NOT call the N+1 per-category method
        verify(skillRepository, never()).countByCategory(any());

        // Seeded zeros for absent categories, non-zero for present
        assertThat(result).containsEntry(SkillCategory.LANGUAGE, 5L);
        assertThat(result).containsEntry(SkillCategory.FRAMEWORK, 3L);
        // All categories are present in the result (zero-seeded)
        assertThat(result).containsKey(SkillCategory.TOOL);
        assertThat(result.get(SkillCategory.TOOL)).isZero();
    }

    @Test
    void countByCategoryReturnsAllCategoriesEvenIfEmpty() {
        when(skillRepository.countGroupedByCategory()).thenReturn(List.of());

        Map<SkillCategory, Long> result = skillService.countByCategory();

        // Every enum value should be present with 0
        for (SkillCategory cat : SkillCategory.values()) {
            assertThat(result).containsKey(cat);
            assertThat(result.get(cat)).isZero();
        }
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private static Skill makeSkill(String name) {
        Skill s = new Skill();
        s.setId(UUID.randomUUID());
        s.setName(name);
        s.setCategory(SkillCategory.LANGUAGE);
        return s;
    }

    private static SkillResponseDto mockResponse() {
        return new SkillResponseDto(UUID.randomUUID(), "Java", null, SkillCategory.LANGUAGE,
                null, false, Instant.now());
    }
}
