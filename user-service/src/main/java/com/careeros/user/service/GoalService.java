package com.careeros.user.service;

import com.careeros.common.pagination.PaginationResponse;
import com.careeros.user.dto.GoalCreateDto;
import com.careeros.user.dto.GoalResponseDto;
import com.careeros.user.dto.GoalSearchDto;
import com.careeros.user.dto.GoalSummaryDto;
import com.careeros.user.dto.GoalUpdateDto;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * Goal use cases. Every operation is scoped to the acting user; cross-user access is impossible
 * (lookups are by id AND user id).
 */
public interface GoalService {

    GoalResponseDto create(UUID userId, GoalCreateDto request);

    GoalResponseDto get(UUID userId, UUID goalId);

    PaginationResponse<GoalSummaryDto> list(UUID userId, Pageable pageable);

    PaginationResponse<GoalSummaryDto> search(UUID userId, GoalSearchDto criteria, Pageable pageable);

    GoalResponseDto update(UUID userId, UUID goalId, GoalUpdateDto request);

    GoalResponseDto updateProgress(UUID userId, UUID goalId, int progressPercent);

    void delete(UUID userId, UUID goalId);
}
