package com.careeros.user.service.impl;

import com.careeros.user.dto.UserAchievementResponseDto;
import com.careeros.user.mapper.UserAchievementMapper;
import com.careeros.user.repository.UserAchievementRepository;
import com.careeros.user.service.AchievementService;
import com.careeros.common.pagination.PaginationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AchievementServiceImpl implements AchievementService {

    private final UserAchievementRepository userAchievementRepository;
    private final UserAchievementMapper userAchievementMapper;

    @Override
    public PaginationResponse<UserAchievementResponseDto> list(UUID userId, Pageable pageable) {
        return PaginationResponse.from(userAchievementRepository.findAllByUserId(userId, pageable)
                .map(userAchievementMapper::toResponse));
    }

    @Override
    public long count(UUID userId) {
        return userAchievementRepository.countByUserId(userId);
    }
}
