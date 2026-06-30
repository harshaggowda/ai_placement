package com.careeros.career.repository;

import com.careeros.career.AbstractPostgresContainerTest;
import com.careeros.career.entity.ProgressSnapshot;
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
class ProgressSnapshotRepositoryTest extends AbstractPostgresContainerTest {

    @Autowired
    private ProgressSnapshotRepository snapshotRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void canSaveAndRetrieveSnapshotWithSkills() {
        UUID userId = UUID.randomUUID();
        
        ProgressSnapshot s = new ProgressSnapshot();
        s.setUserId(userId);
        s.setSnapshotDate(LocalDate.now());
        s.setApplicationsSent(10);
        s.setActiveSkills(List.of("Java", "Spring Boot", "PostgreSQL"));
        
        entityManager.persistAndFlush(s);
        entityManager.clear();
        
        ProgressSnapshot found = snapshotRepository.findByIdAndUserId(s.getId(), userId).orElseThrow();
        
        assertThat(found.getApplicationsSent()).isEqualTo(10);
        assertThat(found.getActiveSkills()).containsExactly("Java", "Spring Boot", "PostgreSQL");
    }
}
