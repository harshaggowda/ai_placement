package com.careeros.auth.service.impl;

import com.careeros.auth.dto.UserSummaryDto;
import com.careeros.auth.mapper.UserMapper;
import com.careeros.auth.repository.UserRepository;
import com.careeros.auth.service.UserQueryService;
import com.careeros.common.pagination.PaginationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Default {@link UserQueryService}. Read-only; maps entities to summary DTOs (soft-deleted users are
 * excluded by the entity filter).
 */
@Service
@RequiredArgsConstructor
public class UserQueryServiceImpl implements UserQueryService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<UserSummaryDto> listUsers(Pageable pageable) {
        return PaginationResponse.from(userRepository.findAll(pageable).map(userMapper::toSummary));
    }
}
