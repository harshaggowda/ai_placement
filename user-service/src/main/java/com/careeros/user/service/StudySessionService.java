package com.careeros.user.service;

import com.careeros.user.dto.StudySessionResponseDto;
import com.careeros.user.dto.StudySessionStartDto;
import com.careeros.user.dto.StudyStatsDto;
import com.careeros.common.pagination.PaginationResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface StudySessionService {
    StudySessionResponseDto start(UUID userId, StudySessionStartDto request);
    StudySessionResponseDto pause(UUID userId, UUID sessionId);
    StudySessionResponseDto resume(UUID userId, UUID sessionId);
    StudySessionResponseDto finish(UUID userId, UUID sessionId);
    StudySessionResponseDto get(UUID userId, UUID sessionId);
    PaginationResponse<StudySessionResponseDto> list(UUID userId, Pageable pageable);
    StudyStatsDto stats(UUID userId);
}
