package com.careeros.career.repository;

import com.careeros.career.AbstractPostgresContainerTest;
import com.careeros.career.entity.Resume;
import com.careeros.career.entity.ResumeStatus;
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
class ResumeRepositoryTest extends AbstractPostgresContainerTest {

    @Autowired
    private ResumeRepository resumeRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void findAllByUserIdReturnsOnlyUserResumes() {
        UUID userId = UUID.randomUUID();
        saveResume(userId, "My Resume 1");
        saveResume(userId, "My Resume 2");
        saveResume(UUID.randomUUID(), "Other User Resume");

        List<Resume> resumes = resumeRepository.findAllByUserId(userId);

        assertThat(resumes).hasSize(2)
                .extracting("title")
                .containsExactlyInAnyOrder("My Resume 1", "My Resume 2");
    }

    @Test
    void softDeletedResumeIsExcludedFromQueries() {
        UUID userId = UUID.randomUUID();
        Resume saved = saveResume(userId, "To be deleted");
        
        resumeRepository.delete(saved);
        entityManager.flush();
        entityManager.clear();
        
        Optional<Resume> found = resumeRepository.findByIdAndUserId(saved.getId(), userId);
        assertThat(found).isEmpty();
        
        List<Resume> all = resumeRepository.findAllByUserId(userId);
        assertThat(all).isEmpty();
    }
    
    private Resume saveResume(UUID userId, String title) {
        Resume r = new Resume();
        r.setUserId(userId);
        r.setTitle(title);
        r.setFileUrl("http://example.com/resume.pdf");
        r.setStatus(ResumeStatus.DRAFT);
        r.setVersionNumber(1);
        r.setIsPublic(false);
        r.setDeleted(false);
        entityManager.persistAndFlush(r);
        return r;
    }
}
