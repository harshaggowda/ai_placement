package com.careeros.career.repository;

import com.careeros.career.AbstractPostgresContainerTest;
import com.careeros.career.entity.Certificate;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {com.careeros.config.JpaAuditingConfig.class, com.careeros.audit.ApplicationAuditorAware.class}))
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class CertificateRepositoryTest extends AbstractPostgresContainerTest {

    @Autowired
    private CertificateRepository certificateRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void canSaveAndRetrieveCertificate() {
        UUID userId = UUID.randomUUID();
        
        Certificate c = new Certificate();
        c.setUserId(userId);
        c.setName("GCP Professional Data Engineer");
        c.setIssuer("Google");
        c.setIssueDate(LocalDate.of(2023, 1, 15));
        entityManager.persistAndFlush(c);
        entityManager.clear();
        
        Certificate found = certificateRepository.findByIdAndUserId(c.getId(), userId).orElseThrow();
        
        assertThat(found.getName()).isEqualTo("GCP Professional Data Engineer");
        assertThat(found.getIssuer()).isEqualTo("Google");
        assertThat(found.getIssueDate()).isEqualTo(LocalDate.of(2023, 1, 15));
    }
}
