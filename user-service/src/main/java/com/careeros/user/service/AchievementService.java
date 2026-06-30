package com.careeros.user.service;

import com.careeros.user.dto.UserAchievementResponseDto;
import com.careeros.common.pagination.PaginationResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface AchievementService {
    PaginationResponse<UserAchievementResponseDto> list(UUID userId, Pageable pageable);
    long count(UUID userId);
}
