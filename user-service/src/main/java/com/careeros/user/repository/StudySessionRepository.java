package com.careeros.user.repository;

import com.careeros.user.entity.StudySession;
import com.careeros.user.entity.StudySessionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Data access for {@link StudySession}. Contracts only. */
@Repository
public interface StudySessionRepository extends JpaRepository<StudySession, UUID> {

    Page<StudySession> findAllByUserId(UUID userId, Pageable pageable);

    Optional<StudySession> findByIdAndUserId(UUID id, UUID userId);

    Optional<StudySession> findFirstByUserIdAndStatusOrderByStartedAtDesc(
            UUID userId, StudySessionStatus status);

    /** Total study minutes in a time window (uses net duration_minutes stored on completion). */
    @Query("""
            SELECT COALESCE(SUM(s.durationMinutes), 0)
            FROM StudySession s
            WHERE s.userId = :userId
              AND s.status = com.careeros.user.entity.StudySessionStatus.COMPLETED
              AND s.startedAt >= :from
              AND s.startedAt < :to
            """)
    long sumDurationMinutesBetween(@Param("userId") UUID userId,
                                   @Param("from") Instant from,
                                   @Param("to") Instant to);

    /** Distinct study dates (UTC truncated) for streak calculation. */
    @Query(value = """
            SELECT DISTINCT DATE(started_at AT TIME ZONE 'UTC')
            FROM study_sessions
            WHERE user_id = :userId
              AND status = 'COMPLETED'
            ORDER BY 1 DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<java.sql.Date> findRecentStudyDates(@Param("userId") UUID userId, @Param("limit") int limit);

    long countByUserIdAndStatus(UUID userId, StudySessionStatus status);
}
