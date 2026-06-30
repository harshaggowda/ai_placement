package com.careeros.user.service.impl;

import com.careeros.exception.ValidationException;
import com.careeros.user.dto.StudySessionResponseDto;
import com.careeros.user.dto.StudySessionStartDto;
import com.careeros.user.entity.StudySession;
import com.careeros.user.entity.StudySessionStatus;
import com.careeros.user.event.StudySessionCompletedEvent;
import com.careeros.user.mapper.StudySessionMapper;
import com.careeros.user.repository.StudySessionRepository;
import com.careeros.user.service.StudyStreakCalculator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link StudySessionServiceImpl}.
 *
 * <p>Covers: start (duplicate active session rejection), pause, resume (paused-time accumulation),
 * finish (duration calculation, event publishing, wrong-status rejection).
 *
 * <p>After Bug Fix #2 a {@link StudyStreakCalculator} mock is needed in the injection set.
 */
@ExtendWith(MockitoExtension.class)
class StudySessionServiceImplTest {

    @Mock StudySessionRepository sessionRepository;
    @Mock StudySessionMapper sessionMapper;
    @Mock StudyStreakCalculator studyStreakCalculator;
    @Mock ApplicationEventPublisher eventPublisher;

    @InjectMocks StudySessionServiceImpl sessionService;

    private final UUID userId = UUID.randomUUID();
    private final UUID sessionId = UUID.randomUUID();

    // -----------------------------------------------------------------------
    // start()
    // -----------------------------------------------------------------------

    @Test
    void startRejectsIfActiveSessionExists() {
        StudySession active = new StudySession();
        when(sessionRepository.findFirstByUserIdAndStatusOrderByStartedAtDesc(userId, StudySessionStatus.ACTIVE))
                .thenReturn(Optional.of(active));

        assertThatThrownBy(() -> sessionService.start(userId, new StudySessionStartDto(null, null)))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void startCreatesSessionWithActiveStatus() {
        when(sessionRepository.findFirstByUserIdAndStatusOrderByStartedAtDesc(userId, StudySessionStatus.ACTIVE))
                .thenReturn(Optional.empty());
        StudySession saved = new StudySession();
        saved.setStatus(StudySessionStatus.ACTIVE);
        when(sessionRepository.save(any(StudySession.class))).thenReturn(saved);
        when(sessionMapper.toResponse(saved)).thenReturn(mockResponse(StudySessionStatus.ACTIVE));

        sessionService.start(userId, new StudySessionStartDto(null, "Study notes"));

        verify(sessionRepository).save(any(StudySession.class));
    }

    // -----------------------------------------------------------------------
    // pause()
    // -----------------------------------------------------------------------

    @Test
    void pauseActiveSessionTransitionsToPaused() {
        StudySession session = activeSession();
        when(sessionRepository.findByIdAndUserId(sessionId, userId)).thenReturn(Optional.of(session));
        when(sessionMapper.toResponse(session)).thenReturn(mockResponse(StudySessionStatus.PAUSED));

        sessionService.pause(userId, sessionId);

        assertThat(session.getStatus()).isEqualTo(StudySessionStatus.PAUSED);
        assertThat(session.getLastPausedAt()).isNotNull();
    }

    @Test
    void pauseAlreadyPausedSessionThrowsValidation() {
        StudySession session = activeSession();
        session.setStatus(StudySessionStatus.PAUSED);
        when(sessionRepository.findByIdAndUserId(sessionId, userId)).thenReturn(Optional.of(session));

        assertThatThrownBy(() -> sessionService.pause(userId, sessionId))
                .isInstanceOf(ValidationException.class);
    }

    // -----------------------------------------------------------------------
    // resume()
    // -----------------------------------------------------------------------

    @Test
    void resumePausedSessionAccumulatesPausedTime() {
        StudySession session = activeSession();
        session.setStatus(StudySessionStatus.PAUSED);
        session.setLastPausedAt(Instant.now().minusSeconds(30));
        when(sessionRepository.findByIdAndUserId(sessionId, userId)).thenReturn(Optional.of(session));
        when(sessionMapper.toResponse(session)).thenReturn(mockResponse(StudySessionStatus.ACTIVE));

        sessionService.resume(userId, sessionId);

        assertThat(session.getStatus()).isEqualTo(StudySessionStatus.ACTIVE);
        assertThat(session.getTotalPausedSeconds()).isGreaterThan(0);
    }

    @Test
    void resumeActiveSessionThrowsValidation() {
        StudySession session = activeSession();
        when(sessionRepository.findByIdAndUserId(sessionId, userId)).thenReturn(Optional.of(session));

        assertThatThrownBy(() -> sessionService.resume(userId, sessionId))
                .isInstanceOf(ValidationException.class);
    }

    // -----------------------------------------------------------------------
    // finish()
    // -----------------------------------------------------------------------

    @Test
    void finishComputesDurationAndPublishesEvent() {
        StudySession session = activeSession();
        session.setStartedAt(Instant.now().minusSeconds(3600)); // 1 hour session
        when(sessionRepository.findByIdAndUserId(sessionId, userId)).thenReturn(Optional.of(session));
        when(sessionMapper.toResponse(session)).thenReturn(mockResponse(StudySessionStatus.COMPLETED));

        sessionService.finish(userId, sessionId);

        assertThat(session.getStatus()).isEqualTo(StudySessionStatus.COMPLETED);
        assertThat(session.getDurationMinutes()).isGreaterThanOrEqualTo(1);
        assertThat(session.getEndedAt()).isNotNull();
        verify(eventPublisher).publishEvent(any(StudySessionCompletedEvent.class));
    }

    @Test
    void finishAlreadyCompletedSessionThrowsValidation() {
        StudySession session = activeSession();
        session.setStatus(StudySessionStatus.COMPLETED);
        when(sessionRepository.findByIdAndUserId(sessionId, userId)).thenReturn(Optional.of(session));

        assertThatThrownBy(() -> sessionService.finish(userId, sessionId))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void finishFromPausedStateStillWorks() {
        StudySession session = activeSession();
        session.setStatus(StudySessionStatus.PAUSED);
        session.setLastPausedAt(Instant.now().minusSeconds(10));
        session.setStartedAt(Instant.now().minusSeconds(600));
        when(sessionRepository.findByIdAndUserId(sessionId, userId)).thenReturn(Optional.of(session));
        when(sessionMapper.toResponse(session)).thenReturn(mockResponse(StudySessionStatus.COMPLETED));

        sessionService.finish(userId, sessionId);

        assertThat(session.getStatus()).isEqualTo(StudySessionStatus.COMPLETED);
        assertThat(session.getDurationMinutes()).isGreaterThanOrEqualTo(1);
    }

    @Test
    void pauseAndResumeAccumulatesPausedTime() {
        StudySession session = activeSession();
        when(sessionRepository.findByIdAndUserId(sessionId, userId)).thenReturn(Optional.of(session));
        when(sessionMapper.toResponse(session)).thenReturn(mockResponse(StudySessionStatus.ACTIVE));

        sessionService.pause(userId, sessionId);
        assertThat(session.getStatus()).isEqualTo(StudySessionStatus.PAUSED);
        assertThat(session.getLastPausedAt()).isNotNull();

        // Simulate that 2 seconds have elapsed while paused
        session.setLastPausedAt(Instant.now().minusSeconds(2));
        sessionService.resume(userId, sessionId);

        assertThat(session.getStatus()).isEqualTo(StudySessionStatus.ACTIVE);
        assertThat(session.getTotalPausedSeconds()).isGreaterThan(0);
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private StudySession activeSession() {
        StudySession s = new StudySession();
        s.setUserId(userId);
        s.setStatus(StudySessionStatus.ACTIVE);
        s.setStartedAt(Instant.now().minusSeconds(300));
        return s;
    }

    private static StudySessionResponseDto mockResponse(StudySessionStatus status) {
        return new StudySessionResponseDto(UUID.randomUUID(), UUID.randomUUID(), null,
                status, Instant.now(), null, null, 0, null, null, Instant.now());
    }
}
