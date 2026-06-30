package com.careeros.user.service.impl;

import com.careeros.exception.ResourceNotFoundException;
import com.careeros.exception.ValidationException;
import com.careeros.user.dto.StudySessionResponseDto;
import com.careeros.user.dto.StudySessionStartDto;
import com.careeros.user.dto.StudyStatsDto;
import com.careeros.user.entity.StudySession;
import com.careeros.user.entity.StudySessionStatus;
import com.careeros.user.event.StudySessionCompletedEvent;
import com.careeros.user.mapper.StudySessionMapper;
import com.careeros.user.repository.StudySessionRepository;
import com.careeros.user.service.StudySessionService;
import com.careeros.user.service.StudyStreakCalculator;
import com.careeros.common.pagination.PaginationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class StudySessionServiceImpl implements StudySessionService {

    private final StudySessionRepository sessionRepository;
    private final StudySessionMapper sessionMapper;
    private final StudyStreakCalculator studyStreakCalculator;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public StudySessionResponseDto start(UUID userId, StudySessionStartDto request) {
        sessionRepository.findFirstByUserIdAndStatusOrderByStartedAtDesc(userId, StudySessionStatus.ACTIVE)
                .ifPresent(s -> {
                    throw new ValidationException("An active study session already exists. Pause or finish it first.");
                });
        StudySession session = new StudySession();
        session.setUserId(userId);
        session.setRoadmapTaskId(request.roadmapTaskId());
        session.setNotes(request.notes());
        session.setStartedAt(Instant.now());
        StudySession saved = sessionRepository.save(session);
        log.info("StudySession started: userId={} sessionId={}", userId, saved.getId());
        return sessionMapper.toResponse(saved);
    }

    @Override
    public StudySessionResponseDto pause(UUID userId, UUID sessionId) {
        StudySession session = requireOwned(userId, sessionId);
        if (session.getStatus() != StudySessionStatus.ACTIVE) {
            throw new ValidationException("Session is not active");
        }
        session.setStatus(StudySessionStatus.PAUSED);
        session.setLastPausedAt(Instant.now());
        return sessionMapper.toResponse(session);
    }

    @Override
    public StudySessionResponseDto resume(UUID userId, UUID sessionId) {
        StudySession session = requireOwned(userId, sessionId);
        if (session.getStatus() != StudySessionStatus.PAUSED) {
            throw new ValidationException("Session is not paused");
        }
        if (session.getLastPausedAt() != null) {
            long pausedSecs = ChronoUnit.SECONDS.between(session.getLastPausedAt(), Instant.now());
            session.setTotalPausedSeconds(session.getTotalPausedSeconds() + pausedSecs);
        }
        session.setStatus(StudySessionStatus.ACTIVE);
        session.setLastPausedAt(null);
        return sessionMapper.toResponse(session);
    }

    @Override
    public StudySessionResponseDto finish(UUID userId, UUID sessionId) {
        StudySession session = requireOwned(userId, sessionId);
        if (session.getStatus() == StudySessionStatus.COMPLETED) {
            throw new ValidationException("Session already completed");
        }
        Instant now = Instant.now();
        if (session.getStatus() == StudySessionStatus.PAUSED && session.getLastPausedAt() != null) {
            long pausedSecs = ChronoUnit.SECONDS.between(session.getLastPausedAt(), now);
            session.setTotalPausedSeconds(session.getTotalPausedSeconds() + pausedSecs);
        }
        session.setEndedAt(now);
        session.setStatus(StudySessionStatus.COMPLETED);
        long totalSecs = ChronoUnit.SECONDS.between(session.getStartedAt(), now) - session.getTotalPausedSeconds();
        int minutes = (int) Math.max(1, totalSecs / 60);
        session.setDurationMinutes(minutes);
        eventPublisher.publishEvent(new StudySessionCompletedEvent(userId, session.getId(), minutes));
        log.info("StudySession completed: userId={} sessionId={} durationMinutes={}", userId, sessionId, minutes);
        return sessionMapper.toResponse(session);
    }

    @Override
    @Transactional(readOnly = true)
    public StudySessionResponseDto get(UUID userId, UUID sessionId) {
        return sessionMapper.toResponse(requireOwned(userId, sessionId));
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<StudySessionResponseDto> list(UUID userId, Pageable pageable) {
        return PaginationResponse.from(sessionRepository.findAllByUserId(userId, pageable)
                .map(sessionMapper::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public StudyStatsDto stats(UUID userId) {
        Instant now = Instant.now();
        ZonedDateTime todayStart = ZonedDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.DAYS);
        ZonedDateTime weekStart = todayStart.minusDays(todayStart.getDayOfWeek().getValue() - 1);
        ZonedDateTime monthStart = todayStart.withDayOfMonth(1);

        long today = sessionRepository.sumDurationMinutesBetween(userId, todayStart.toInstant(), todayStart.plusDays(1).toInstant());
        long week = sessionRepository.sumDurationMinutesBetween(userId, weekStart.toInstant(), todayStart.plusDays(1).toInstant());
        long month = sessionRepository.sumDurationMinutesBetween(userId, monthStart.toInstant(), todayStart.plusDays(1).toInstant());
        long total = sessionRepository.sumDurationMinutesBetween(userId, Instant.EPOCH, now.plusSeconds(1));
        long sessions = sessionRepository.countByUserIdAndStatus(userId, StudySessionStatus.COMPLETED);
        int streak = studyStreakCalculator.calculate(userId);

        return new StudyStatsDto(today, week, month, total, streak, sessions);
    }

    private StudySession requireOwned(UUID userId, UUID sessionId) {
        return sessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("StudySession", "id", sessionId));
    }
}
