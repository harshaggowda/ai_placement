package com.careeros.career.repository;

import com.careeros.career.AbstractPostgresContainerTest;
import com.careeros.career.entity.Company;
import com.careeros.career.entity.CompanyPreparation;
import com.careeros.career.entity.PreparationStatus;
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
class CompanyPreparationRepositoryTest extends AbstractPostgresContainerTest {

    @Autowired
    private CompanyPreparationRepository preparationRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void findByUserIdAndCompanyIdWorksWithEntityGraph() {
        UUID userId = UUID.randomUUID();
        
        Company c = new Company();
        c.setName("Google");
        c.setDeleted(false);
        entityManager.persist(c);
        
        CompanyPreparation cp = new CompanyPreparation();
        cp.setUserId(userId);
        cp.setCompany(c);
        cp.setPreparationStatus(PreparationStatus.READY);
        entityManager.persistAndFlush(cp);
        entityManager.clear();
        
        Optional<CompanyPreparation> found = preparationRepository.findByUserIdAndCompanyId(userId, c.getId());
        
        assertThat(found).isPresent();
        assertThat(found.get().getCompany().getName()).isEqualTo("Google");
    }
}
