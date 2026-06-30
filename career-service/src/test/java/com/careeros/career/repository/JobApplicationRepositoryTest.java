package com.careeros.career.repository;

import com.careeros.career.AbstractPostgresContainerTest;
import com.careeros.career.entity.ApplicationStatus;
import com.careeros.career.entity.Company;
import com.careeros.career.entity.JobApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {com.careeros.config.JpaAuditingConfig.class, com.careeros.audit.ApplicationAuditorAware.class}))
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class JobApplicationRepositoryTest extends AbstractPostgresContainerTest {

    @Autowired
    private JobApplicationRepository applicationRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void findByIdAndUserIdWorksWithEntityGraph() {
        UUID userId = UUID.randomUUID();
        
        Company c = new Company();
        c.setName("Netflix");
        c.setDeleted(false);
        entityManager.persist(c);
        
        JobApplication app = new JobApplication();
        app.setUserId(userId);
        app.setCompany(c);
        app.setRole("Backend Engineer");
        app.setStatus(ApplicationStatus.APPLIED);
        entityManager.persistAndFlush(app);
        entityManager.clear();
        
        Optional<JobApplication> found = applicationRepository.findByIdAndUserId(app.getId(), userId);
        
        assertThat(found).isPresent();
        assertThat(found.get().getCompany().getName()).isEqualTo("Netflix");
    }
}
