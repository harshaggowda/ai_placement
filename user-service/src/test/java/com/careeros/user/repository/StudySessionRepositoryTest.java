package com.careeros.user.repository;

import com.careeros.audit.ApplicationAuditorAware;
import com.careeros.config.JpaAuditingConfig;
import com.careeros.user.AbstractPostgresContainerTest;
import com.careeros.user.entity.StudySession;
import com.careeros.user.entity.StudySessionStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Repository slice for {@link StudySessionRepository}: tests the streak date query,
 * duration sum window, ownership lookup, and count.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Import({JpaAuditingConfig.class, ApplicationAuditorAware.class})
@TestPropertySource(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class StudySessionRepositoryTest extends AbstractPostgresContainerTest {

    @Autowired private StudySessionRepository sessionRepository;
    @Autowired private TestEntityManager entityManager;

    @Test
    void findByIdAndUserIdReturnsSessionForCorrectOwner() {
        UUID userId = UUID.randomUUID();
        StudySession session = saveCompleted(userId, 30);

        Optional<StudySession> found = sessionRepository.findByIdAndUserId(session.getId(), userId);
        assertThat(found).isPresent();
    }

    @Test
    void findByIdAndUserIdReturnsEmptyForWrongOwner() {
        UUID owner = UUID.randomUUID();
        StudySession session = saveCompleted(owner, 30);

        Optional<StudySession> found = sessionRepository.findByIdAndUserId(session.getId(), UUID.randomUUID());
        assertThat(found).isEmpty();
    }

    @Test
    void countByUserIdAndStatusCountsOnlyMatchingStatus() {
        UUID userId = UUID.randomUUID();
        saveCompleted(userId, 30);
        saveCompleted(userId, 60);
        saveActive(userId);
        entityManager.flush();
        entityManager.clear();

        long completedCount = sessionRepository.countByUserIdAndStatus(userId, StudySessionStatus.COMPLETED);
        long activeCount = sessionRepository.countByUserIdAndStatus(userId, StudySessionStatus.ACTIVE);

        assertThat(completedCount).isEqualTo(2);
        assertThat(activeCount).isEqualTo(1);
    }

    @Test
    void sumDurationMinutesBetweenSumsWithinWindow() {
        UUID userId = UUID.randomUUID();
        Instant now = Instant.now();
        saveCompleted(userId, 30);  // started "now-3600" by default
        saveCompleted(userId, 45);
        entityManager.flush();
        entityManager.clear();

        long sum = sessionRepository.sumDurationMinutesBetween(userId,
                now.minus(1, ChronoUnit.DAYS), now.plus(1, ChronoUnit.DAYS));
        assertThat(sum).isEqualTo(75);
    }

    @Test
    void sumDurationMinutesBetweenReturnsZeroOutsideWindow() {
        UUID userId = UUID.randomUUID();
        saveCompleted(userId, 30);
        entityManager.flush();
        entityManager.clear();

        // Window entirely in the future
        Instant future = Instant.now().plus(1, ChronoUnit.DAYS);
        long sum = sessionRepository.sumDurationMinutesBetween(userId,
                future, future.plus(1, ChronoUnit.DAYS));
        assertThat(sum).isZero();
    }

    @Test
    void findRecentStudyDatesReturnsDistinctDates() {
        UUID userId = UUID.randomUUID();
        // Two sessions on the same day
        saveCompleted(userId, 30);
        saveCompleted(userId, 45);
        entityManager.flush();
        entityManager.clear();

        List<java.sql.Date> dates = sessionRepository.findRecentStudyDates(userId, 90);
        // Should be 1 distinct date even though 2 sessions
        assertThat(dates).hasSize(1);
    }

    @Test
    void findFirstActiveSessionReturnsLatest() {
        UUID userId = UUID.randomUUID();
        StudySession s1 = saveActive(userId);
        entityManager.flush();

        Optional<StudySession> found = sessionRepository
                .findFirstByUserIdAndStatusOrderByStartedAtDesc(userId, StudySessionStatus.ACTIVE);
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(s1.getId());
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private StudySession saveCompleted(UUID userId, int durationMinutes) {
        StudySession s = new StudySession();
        s.setUserId(userId);
        s.setStatus(StudySessionStatus.COMPLETED);
        s.setStartedAt(Instant.now().minus(1, ChronoUnit.HOURS));
        s.setEndedAt(Instant.now());
        s.setDurationMinutes(durationMinutes);
        return sessionRepository.save(s);
    }

    private StudySession saveActive(UUID userId) {
        StudySession s = new StudySession();
        s.setUserId(userId);
        s.setStatus(StudySessionStatus.ACTIVE);
        s.setStartedAt(Instant.now());
        return sessionRepository.save(s);
    }
}
