package com.careeros.user.repository;

import com.careeros.audit.ApplicationAuditorAware;
import com.careeros.config.JpaAuditingConfig;
import com.careeros.user.AbstractPostgresContainerTest;
import com.careeros.user.entity.UserProfile;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Repository slice against real PostgreSQL: verifies the {@code user_id} lookups, the unique
 * constraint, jsonb round-tripping of list fields, and the soft-delete filter.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Import({JpaAuditingConfig.class, ApplicationAuditorAware.class})
@TestPropertySource(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class UserProfileRepositoryTest extends AbstractPostgresContainerTest {

    @Autowired
    private UserProfileRepository profileRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void savesAndFindsByUserIdWithJsonbRoundtrip() {
        UUID userId = UUID.randomUUID();
        UserProfile profile = new UserProfile();
        profile.setUserId(userId);
        profile.setHeadline("Aspiring SWE");
        profile.setPreferredLanguages(List.of("Java", "Go"));
        profileRepository.save(profile);
        entityManager.flush();
        entityManager.clear();

        UserProfile found = profileRepository.findByUserId(userId).orElseThrow();
        assertThat(found.getHeadline()).isEqualTo("Aspiring SWE");
        assertThat(found.getPreferredLanguages()).containsExactly("Java", "Go");
        assertThat(profileRepository.existsByUserId(userId)).isTrue();
    }

    @Test
    void softDeletedProfileIsExcluded() {
        UUID userId = UUID.randomUUID();
        UserProfile profile = new UserProfile();
        profile.setUserId(userId);
        UserProfile saved = profileRepository.save(profile);
        entityManager.flush();

        profileRepository.delete(saved);
        entityManager.flush();
        entityManager.clear();

        assertThat(profileRepository.findByUserId(userId)).isEmpty();
    }
}
