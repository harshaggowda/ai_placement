package com.careeros.career.repository;

import com.careeros.career.AbstractPostgresContainerTest;
import com.careeros.career.entity.ApplicationStatus;
import com.careeros.career.entity.Company;
import com.careeros.career.entity.Interview;
import com.careeros.career.entity.InterviewFormat;
import com.careeros.career.entity.InterviewStatus;
import com.careeros.career.entity.InterviewType;
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
class InterviewRepositoryTest extends AbstractPostgresContainerTest {

    @Autowired
    private InterviewRepository interviewRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void findAllByApplicationIdAndUserIdWorksWithEntityGraph() {
        UUID userId = UUID.randomUUID();
        
        Company c = new Company();
        c.setName("Amazon");
        c.setDeleted(false);
        entityManager.persist(c);
        
        JobApplication app = new JobApplication();
        app.setUserId(userId);
        app.setCompany(c);
        app.setRole("SDE II");
        app.setStatus(ApplicationStatus.INTERVIEWING);
        entityManager.persist(app);
        
        Interview i1 = new Interview();
        i1.setUserId(userId);
        i1.setApplication(app);
        i1.setType(InterviewType.TECHNICAL);
        i1.setFormat(InterviewFormat.VIRTUAL);
        i1.setStatus(InterviewStatus.SCHEDULED);
        entityManager.persist(i1);

        Interview i2 = new Interview();
        i2.setUserId(userId);
        i2.setApplication(app);
        i2.setType(InterviewType.HR);
        i2.setFormat(InterviewFormat.PHONE);
        i2.setStatus(InterviewStatus.PASSED);
        entityManager.persist(i2);
        
        entityManager.flush();
        entityManager.clear();
        
        List<Interview> interviews = interviewRepository.findAllByApplicationIdAndUserId(app.getId(), userId);
        
        assertThat(interviews).hasSize(2);
        // Checking entity graph eagerly loaded Company
        assertThat(interviews.get(0).getApplication().getCompany().getName()).isEqualTo("Amazon");
    }
}
