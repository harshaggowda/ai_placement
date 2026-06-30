package com.careeros.career.repository;

import com.careeros.career.AbstractPostgresContainerTest;
import com.careeros.career.dto.PlannerTask;
import com.careeros.career.entity.DailyPlanner;
import com.careeros.career.entity.PlannerTaskStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {com.careeros.config.JpaAuditingConfig.class, com.careeros.audit.ApplicationAuditorAware.class}))
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class DailyPlannerRepositoryTest extends AbstractPostgresContainerTest {

    @Autowired
    private DailyPlannerRepository plannerRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void canSaveAndRetrievePlannerWithTasks() {
        UUID userId = UUID.randomUUID();
        
        PlannerTask t1 = new PlannerTask();
        t1.setId("1");
        t1.setTitle("LeetCode DP");
        t1.setStatus(PlannerTaskStatus.IN_PROGRESS);
        
        DailyPlanner p = new DailyPlanner();
        p.setUserId(userId);
        p.setTargetDate(LocalDate.now());
        p.setTasks(List.of(t1));
        
        entityManager.persistAndFlush(p);
        entityManager.clear();
        
        DailyPlanner found = plannerRepository.findByIdAndUserId(p.getId(), userId).orElseThrow();
        
        assertThat(found.getTasks()).hasSize(1);
        assertThat(found.getTasks().get(0).getTitle()).isEqualTo("LeetCode DP");
    }
}
