package com.careeros.user.service.impl;

import com.careeros.common.pagination.PaginationResponse;
import com.careeros.exception.ResourceNotFoundException;
import com.careeros.exception.ValidationException;
import com.careeros.user.dto.GoalCreateDto;
import com.careeros.user.dto.GoalResponseDto;
import com.careeros.user.dto.GoalSearchDto;
import com.careeros.user.dto.GoalSummaryDto;
import com.careeros.user.dto.GoalUpdateDto;
import com.careeros.user.entity.Goal;
import com.careeros.user.entity.GoalStatus;
import com.careeros.user.event.GoalCompletedEvent;
import com.careeros.user.mapper.GoalMapper;
import com.careeros.user.repository.GoalRepository;
import com.careeros.user.repository.GoalSpecifications;
import com.careeros.user.service.GoalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Default {@link GoalService}. Enforces ownership via id+userId lookups, validates progress bounds,
 * and applies the completion rule (status COMPLETED ⇔ progress 100 + completion timestamp + event).
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class GoalServiceImpl implements GoalService {

    private static final int MIN_PROGRESS = 0;
    private static final int MAX_PROGRESS = 100;

    private final GoalRepository goalRepository;
    private final GoalMapper goalMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public GoalResponseDto create(UUID userId, GoalCreateDto request) {
        Goal goal = goalMapper.toEntity(request);
        goal.setUserId(userId);
        validateProgress(goal.getProgressPercent());
        Goal saved = goalRepository.save(goal);
        log.info("Goal created: userId={} goalId={}", userId, saved.getId());
        return goalMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public GoalResponseDto get(UUID userId, UUID goalId) {
        return goalMapper.toResponse(requireOwnedGoal(userId, goalId));
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<GoalSummaryDto> list(UUID userId, Pageable pageable) {
        return PaginationResponse.from(
                goalRepository.findAllByUserId(userId, pageable).map(goalMapper::toSummary));
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<GoalSummaryDto> search(UUID userId, GoalSearchDto criteria, Pageable pageable) {
        Specification<Goal> spec = Specification.where(GoalSpecifications.ownedBy(userId));
        if (criteria.status() != null) {
            spec = spec.and(GoalSpecifications.hasStatus(criteria.status()));
        }
        if (criteria.category() != null) {
            spec = spec.and(GoalSpecifications.hasCategory(criteria.category()));
        }
        if (criteria.priority() != null) {
            spec = spec.and(GoalSpecifications.hasPriority(criteria.priority()));
        }
        return PaginationResponse.from(goalRepository.findAll(spec, pageable).map(goalMapper::toSummary));
    }

    @Override
    public GoalResponseDto update(UUID userId, UUID goalId, GoalUpdateDto request) {
        Goal goal = requireOwnedGoal(userId, goalId);
        GoalStatus previousStatus = goal.getStatus();
        goalMapper.updateEntity(request, goal);
        validateProgress(goal.getProgressPercent());
        applyCompletionRules(goal, previousStatus);
        log.info("Goal updated: userId={} goalId={} status={}", userId, goalId, goal.getStatus());
        return goalMapper.toResponse(goal);
    }

    @Override
    public GoalResponseDto updateProgress(UUID userId, UUID goalId, int progressPercent) {
        validateProgress(progressPercent);
        Goal goal = requireOwnedGoal(userId, goalId);
        GoalStatus previousStatus = goal.getStatus();
        goal.setProgressPercent(progressPercent);
        if (progressPercent >= MAX_PROGRESS) {
            markCompleted(goal, previousStatus);
        } else if (progressPercent > MIN_PROGRESS && goal.getStatus() == GoalStatus.NOT_STARTED) {
            goal.setStatus(GoalStatus.IN_PROGRESS);
        }
        return goalMapper.toResponse(goal);
    }

    @Override
    public void delete(UUID userId, UUID goalId) {
        goalRepository.delete(requireOwnedGoal(userId, goalId));
        log.info("Goal soft-deleted: userId={} goalId={}", userId, goalId);
    }

    private Goal requireOwnedGoal(UUID userId, UUID goalId) {
        return goalRepository.findByIdAndUserId(goalId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Goal", "id", goalId));
    }

    /** Reconcile status and progress when a goal is (or becomes) complete. */
    private void applyCompletionRules(Goal goal, GoalStatus previousStatus) {
        if (goal.getStatus() == GoalStatus.COMPLETED) {
            goal.setProgressPercent(MAX_PROGRESS);
            if (previousStatus != GoalStatus.COMPLETED) {
                stampCompletion(goal);
            }
        } else if (goal.getProgressPercent() >= MAX_PROGRESS) {
            markCompleted(goal, previousStatus);
        }
    }

    private void markCompleted(Goal goal, GoalStatus previousStatus) {
        goal.setStatus(GoalStatus.COMPLETED);
        goal.setProgressPercent(MAX_PROGRESS);
        if (previousStatus != GoalStatus.COMPLETED) {
            stampCompletion(goal);
        }
    }

    private void stampCompletion(Goal goal) {
        goal.setCompletedAt(Instant.now());
        eventPublisher.publishEvent(new GoalCompletedEvent(goal.getUserId(), goal.getId(), goal.getTitle()));
        log.info("Goal completed: userId={} goalId={}", goal.getUserId(), goal.getId());
    }

    private void validateProgress(int progressPercent) {
        if (progressPercent < MIN_PROGRESS || progressPercent > MAX_PROGRESS) {
            throw new ValidationException("progressPercent must be between 0 and 100");
        }
    }
}
