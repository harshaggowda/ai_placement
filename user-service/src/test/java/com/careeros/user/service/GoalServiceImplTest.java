package com.careeros.user.service.impl;

import com.careeros.exception.ResourceNotFoundException;
import com.careeros.exception.ValidationException;
import com.careeros.user.dto.GoalCreateDto;
import com.careeros.user.dto.GoalResponseDto;
import com.careeros.user.dto.GoalSearchDto;
import com.careeros.user.entity.Goal;
import com.careeros.user.entity.GoalCategory;
import com.careeros.user.entity.GoalPriority;
import com.careeros.user.entity.GoalStatus;
import com.careeros.user.event.GoalCompletedEvent;
import com.careeros.user.mapper.GoalMapper;
import com.careeros.user.repository.GoalRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link GoalServiceImpl}. Uses Mockito to isolate the service from JPA and events.
 */
@ExtendWith(MockitoExtension.class)
class GoalServiceImplTest {

    @Mock GoalRepository goalRepository;
    @Mock GoalMapper goalMapper;
    @Mock ApplicationEventPublisher eventPublisher;

    @InjectMocks GoalServiceImpl goalService;

    private final UUID userId = UUID.randomUUID();
    private final UUID goalId = UUID.randomUUID();

    // -----------------------------------------------------------------------
    // create()
    // -----------------------------------------------------------------------

    @Test
    void createAssignsOwnerAndPersists() {
        Goal goal = new Goal();
        GoalCreateDto dto = new GoalCreateDto("Land a job", null, GoalCategory.CAREER, null, null, null);
        when(goalMapper.toEntity(dto)).thenReturn(goal);
        when(goalRepository.save(any(Goal.class))).thenAnswer(inv -> inv.getArgument(0));
        when(goalMapper.toResponse(any(Goal.class))).thenReturn(mockResponse());

        goalService.create(userId, dto);

        assertThat(goal.getUserId()).isEqualTo(userId);
        verify(goalRepository).save(goal);
    }

    @Test
    void createWithProgressOver100ThrowsValidation() {
        Goal goal = new Goal();
        goal.setProgressPercent(150);
        GoalCreateDto dto = new GoalCreateDto("Bad", null, GoalCategory.CAREER, null, 150, null);
        when(goalMapper.toEntity(dto)).thenReturn(goal);

        assertThatThrownBy(() -> goalService.create(userId, dto))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void createWithNegativeProgressThrowsValidation() {
        Goal goal = new Goal();
        goal.setProgressPercent(-1);
        GoalCreateDto dto = new GoalCreateDto("Bad", null, GoalCategory.CAREER, null, -1, null);
        when(goalMapper.toEntity(dto)).thenReturn(goal);

        assertThatThrownBy(() -> goalService.create(userId, dto))
                .isInstanceOf(ValidationException.class);
    }

    // -----------------------------------------------------------------------
    // get()
    // -----------------------------------------------------------------------

    @Test
    void getRejectsCrossUserAccessAsNotFound() {
        when(goalRepository.findByIdAndUserId(goalId, userId)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> goalService.get(userId, goalId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getHappyPath() {
        Goal goal = makeGoal(GoalStatus.IN_PROGRESS, 50);
        when(goalRepository.findByIdAndUserId(goalId, userId)).thenReturn(Optional.of(goal));
        when(goalMapper.toResponse(goal)).thenReturn(mockResponse());

        goalService.get(userId, goalId);

        verify(goalMapper).toResponse(goal);
    }

    // -----------------------------------------------------------------------
    // updateProgress()
    // -----------------------------------------------------------------------

    @Test
    void updateProgressTo100CompletesGoalAndPublishesEvent() {
        Goal goal = makeGoal(GoalStatus.IN_PROGRESS, 50);
        when(goalRepository.findByIdAndUserId(goalId, userId)).thenReturn(Optional.of(goal));
        when(goalMapper.toResponse(goal)).thenReturn(mockResponse());

        goalService.updateProgress(userId, goalId, 100);

        assertThat(goal.getStatus()).isEqualTo(GoalStatus.COMPLETED);
        assertThat(goal.getProgressPercent()).isEqualTo(100);
        assertThat(goal.getCompletedAt()).isNotNull();
        verify(eventPublisher).publishEvent(any(GoalCompletedEvent.class));
    }

    @Test
    void updateProgressTo50MovesToInProgress() {
        Goal goal = makeGoal(GoalStatus.NOT_STARTED, 0);
        when(goalRepository.findByIdAndUserId(goalId, userId)).thenReturn(Optional.of(goal));
        when(goalMapper.toResponse(goal)).thenReturn(mockResponse());

        goalService.updateProgress(userId, goalId, 50);

        assertThat(goal.getStatus()).isEqualTo(GoalStatus.IN_PROGRESS);
        assertThat(goal.getProgressPercent()).isEqualTo(50);
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void updateProgressTo0KeepsNotStarted() {
        Goal goal = makeGoal(GoalStatus.NOT_STARTED, 0);
        when(goalRepository.findByIdAndUserId(goalId, userId)).thenReturn(Optional.of(goal));
        when(goalMapper.toResponse(goal)).thenReturn(mockResponse());

        goalService.updateProgress(userId, goalId, 0);

        assertThat(goal.getStatus()).isEqualTo(GoalStatus.NOT_STARTED);
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void updateProgressRejectsOutOfRange_over100() {
        assertThatThrownBy(() -> goalService.updateProgress(userId, goalId, 150))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void updateProgressRejectsOutOfRange_negative() {
        assertThatThrownBy(() -> goalService.updateProgress(userId, goalId, -1))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void updateProgressTo100OnAlreadyCompletedGoalDoesNotFireEventAgain() {
        Goal goal = makeGoal(GoalStatus.COMPLETED, 100);
        goal.setCompletedAt(java.time.Instant.now().minusSeconds(600));
        when(goalRepository.findByIdAndUserId(goalId, userId)).thenReturn(Optional.of(goal));
        when(goalMapper.toResponse(goal)).thenReturn(mockResponse());

        goalService.updateProgress(userId, goalId, 100);

        // completedAt should NOT be reset
        verify(eventPublisher, never()).publishEvent(any());
    }

    // -----------------------------------------------------------------------
    // delete()
    // -----------------------------------------------------------------------

    @Test
    void deleteWithWrongUserThrowsNotFound() {
        when(goalRepository.findByIdAndUserId(goalId, userId)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> goalService.delete(userId, goalId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteHappyPath() {
        Goal goal = makeGoal(GoalStatus.IN_PROGRESS, 50);
        when(goalRepository.findByIdAndUserId(goalId, userId)).thenReturn(Optional.of(goal));

        goalService.delete(userId, goalId);

        verify(goalRepository).delete(goal);
    }

    // -----------------------------------------------------------------------
    // update()
    // -----------------------------------------------------------------------

    @Test
    void updateWithStatusCompletedForces100Progress() {
        Goal goal = makeGoal(GoalStatus.IN_PROGRESS, 50);
        when(goalRepository.findByIdAndUserId(goalId, userId)).thenReturn(Optional.of(goal));
        when(goalMapper.toResponse(goal)).thenReturn(mockResponse());
        // Simulate mapper setting status to COMPLETED
        doAnswer(inv -> {
            goal.setStatus(GoalStatus.COMPLETED);
            return null;
        }).when(goalMapper).updateEntity(any(), any());

        goalService.update(userId, goalId, new com.careeros.user.dto.GoalUpdateDto(
                null, null, null, GoalStatus.COMPLETED, null, null, null));

        assertThat(goal.getProgressPercent()).isEqualTo(100);
        assertThat(goal.getCompletedAt()).isNotNull();
        verify(eventPublisher).publishEvent(any(GoalCompletedEvent.class));
    }

    @Test
    void updateAlreadyCompletedGoalDoesNotFireSecondEvent() {
        Goal goal = makeGoal(GoalStatus.COMPLETED, 100);
        goal.setCompletedAt(java.time.Instant.now().minusSeconds(600));
        when(goalRepository.findByIdAndUserId(goalId, userId)).thenReturn(Optional.of(goal));
        when(goalMapper.toResponse(goal)).thenReturn(mockResponse());
        // Mapper doesn't change status
        doNothing().when(goalMapper).updateEntity(any(), any());

        goalService.update(userId, goalId, new com.careeros.user.dto.GoalUpdateDto(
                "New Title", null, null, null, null, null, null));

        verify(eventPublisher, never()).publishEvent(any());
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private Goal makeGoal(GoalStatus status, int progress) {
        Goal goal = new Goal();
        goal.setUserId(userId);
        goal.setTitle("Finish DSA");
        goal.setCategory(GoalCategory.SKILL);
        goal.setStatus(status);
        goal.setProgressPercent(progress);
        goal.setPriority(GoalPriority.MEDIUM);
        return goal;
    }

    private static GoalResponseDto mockResponse() {
        return new GoalResponseDto(UUID.randomUUID(), UUID.randomUUID(), "t", null,
                GoalCategory.CAREER, GoalStatus.IN_PROGRESS, null, 0, null, null, null, null);
    }
}
