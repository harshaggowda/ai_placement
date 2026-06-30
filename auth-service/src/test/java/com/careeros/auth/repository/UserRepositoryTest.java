package com.careeros.auth.repository;

import com.careeros.auth.AbstractPostgresContainerTest;
import com.careeros.auth.entity.User;
import com.careeros.auth.entity.UserStatus;
import com.careeros.audit.ApplicationAuditorAware;
import com.careeros.config.JpaAuditingConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Repository slice test against a real PostgreSQL. Flyway is disabled and Hibernate creates the
 * schema (create-drop) so the test exercises the JPA mappings and derived queries directly,
 * including the soft-delete filter. Auditing config is imported so the {@code NOT NULL} audit
 * columns are populated.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Import({JpaAuditingConfig.class, ApplicationAuditorAware.class})
@TestPropertySource(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class UserRepositoryTest extends AbstractPostgresContainerTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void savesAndFindsUserByEmail() {
        userRepository.save(newUser("found@careeros.ai"));
        entityManager.flush();
        entityManager.clear();

        assertThat(userRepository.findByEmail("found@careeros.ai")).isPresent();
        assertThat(userRepository.existsByEmail("found@careeros.ai")).isTrue();
        assertThat(userRepository.existsByEmail("missing@careeros.ai")).isFalse();
    }

    @Test
    void softDeletedUsersAreExcludedFromQueries() {
        User saved = userRepository.save(newUser("deleted@careeros.ai"));
        entityManager.flush();

        userRepository.delete(saved);
        entityManager.flush();
        entityManager.clear();

        assertThat(userRepository.findByEmail("deleted@careeros.ai")).isEmpty();
    }

    private User newUser(String email) {
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash("hashed");
        user.setStatus(UserStatus.ACTIVE);
        user.setEmailVerified(true);
        return user;
    }
}
