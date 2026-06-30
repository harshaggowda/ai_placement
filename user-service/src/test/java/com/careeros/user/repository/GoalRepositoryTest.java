package com.careeros.user.repository;

import com.careeros.audit.ApplicationAuditorAware;
import com.careeros.config.JpaAuditingConfig;
import com.careeros.user.AbstractPostgresContainerTest;
import com.careeros.user.entity.Goal;
import com.careeros.user.entity.GoalCategory;
import com.careeros.user.entity.GoalPriority;
import com.careeros.user.entity.GoalStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Repository slice for {@link GoalRepository}: tests soft-delete filter, ownership query,
 * Specification-based filtering, and {@code countByUserIdAndStatus}.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Import({JpaAuditingConfig.class, ApplicationAuditorAware.class})
@TestPropertySource(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class GoalRepositoryTest extends AbstractPostgresContainerTest {

    @Autowired private GoalRepository goalRepository;
    @Autowired private TestEntityManager entityManager;

    @Test
    void findByIdAndUserIdReturnsGoalForCorrectOwner() {
        UUID userId = UUID.randomUUID();
        Goal goal = saveGoal(userId, GoalStatus.NOT_STARTED, 0);

        Optional<Goal> found = goalRepository.findByIdAndUserId(goal.getId(), userId);
        assertThat(found).isPresent();
    }

    @Test
    void findByIdAndUserIdReturnsEmptyForWrongOwner() {
        UUID owner = UUID.randomUUID();
        UUID intruder = UUID.randomUUID();
        Goal goal = saveGoal(owner, GoalStatus.NOT_STARTED, 0);

        Optional<Goal> found = goalRepository.findByIdAndUserId(goal.getId(), intruder);
        assertThat(found).isEmpty();
    }

    @Test
    void softDeletedGoalIsExcludedFromQueries() {
        UUID userId = UUID.randomUUID();
        Goal goal = saveGoal(userId, GoalStatus.IN_PROGRESS, 50);
        entityManager.flush();

        goalRepository.delete(goal);
        entityManager.flush();
        entityManager.clear();

        assertThat(goalRepository.findByIdAndUserId(goal.getId(), userId)).isEmpty();
        assertThat(goalRepository.findAllByUserId(userId, PageRequest.of(0, 10)).getContent()).isEmpty();
    }

    @Test
    void countByUserIdAndStatusCountsCorrectly() {
        UUID userId = UUID.randomUUID();
        saveGoal(userId, GoalStatus.COMPLETED, 100);
        saveGoal(userId, GoalStatus.COMPLETED, 100);
        saveGoal(userId, GoalStatus.IN_PROGRESS, 50);

        long completed = goalRepository.countByUserIdAndStatus(userId, GoalStatus.COMPLETED);
        long inProgress = goalRepository.countByUserIdAndStatus(userId, GoalStatus.IN_PROGRESS);

        assertThat(completed).isEqualTo(2);
        assertThat(inProgress).isEqualTo(1);
    }

    @Test
    void findAllByUserIdReturnsPaginatedResults() {
        UUID userId = UUID.randomUUID();
        for (int i = 0; i < 5; i++) {
            saveGoal(userId, GoalStatus.NOT_STARTED, 0);
        }
        entityManager.flush();
        entityManager.clear();

        Page<Goal> page = goalRepository.findAllByUserId(userId, PageRequest.of(0, 3));

        assertThat(page.getContent()).hasSize(3);
        assertThat(page.getTotalElements()).isEqualTo(5);
    }

    @Test
    void specificationFiltersByStatus() {
        UUID userId = UUID.randomUUID();
        saveGoal(userId, GoalStatus.COMPLETED, 100);
        saveGoal(userId, GoalStatus.IN_PROGRESS, 50);
        entityManager.flush();
        entityManager.clear();

        Specification<Goal> spec = Specification.where(GoalSpecifications.ownedBy(userId))
                .and(GoalSpecifications.hasStatus(GoalStatus.COMPLETED));
        Page<Goal> page = goalRepository.findAll(spec, PageRequest.of(0, 10));

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getStatus()).isEqualTo(GoalStatus.COMPLETED);
    }

    @Test
    void specificationFiltersByCategory() {
        UUID userId = UUID.randomUUID();
        saveGoalWithCategory(userId, GoalCategory.CAREER);
        saveGoalWithCategory(userId, GoalCategory.SKILL);
        entityManager.flush();
        entityManager.clear();

        Specification<Goal> spec = Specification.where(GoalSpecifications.ownedBy(userId))
                .and(GoalSpecifications.hasCategory(GoalCategory.CAREER));
        Page<Goal> page = goalRepository.findAll(spec, PageRequest.of(0, 10));

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getCategory()).isEqualTo(GoalCategory.CAREER);
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private Goal saveGoal(UUID userId, GoalStatus status, int progress) {
        Goal g = buildGoal(userId, GoalCategory.CAREER, status, progress);
        return goalRepository.save(g);
    }

    private Goal saveGoalWithCategory(UUID userId, GoalCategory category) {
        Goal g = buildGoal(userId, category, GoalStatus.NOT_STARTED, 0);
        return goalRepository.save(g);
    }

    private Goal buildGoal(UUID userId, GoalCategory category, GoalStatus status, int progress) {
        Goal g = new Goal();
        g.setUserId(userId);
        g.setTitle("Test goal");
        g.setCategory(category);
        g.setStatus(status);
        g.setProgressPercent(progress);
        g.setPriority(GoalPriority.MEDIUM);
        return g;
    }
}
