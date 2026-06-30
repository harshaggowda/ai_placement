package com.careeros.user.service;

import com.careeros.user.repository.SkillRepository;
import com.careeros.user.AbstractPostgresContainerTest;
import com.careeros.user.entity.Skill;
import com.careeros.user.entity.SkillCategory;
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

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Repository slice test for the {@code countGroupedByCategory} GROUP BY query — validates Bug Fix #3.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Import({JpaAuditingConfig.class, ApplicationAuditorAware.class})
@TestPropertySource(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class SkillRepositoryGroupByTest extends AbstractPostgresContainerTest {

    @Autowired
    private com.careeros.user.repository.SkillRepository skillRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void countGroupedByCategoryReturnsSingleRowPerCategory() {
        save("Java", SkillCategory.LANGUAGE);
        save("Go", SkillCategory.LANGUAGE);
        save("Spring", SkillCategory.FRAMEWORK);
        entityManager.flush();
        entityManager.clear();

        List<Object[]> rows = skillRepository.countGroupedByCategory();

        Map<SkillCategory, Long> result = rows.stream()
                .collect(Collectors.toMap(r -> (SkillCategory) r[0], r -> (Long) r[1]));

        assertThat(result).containsEntry(SkillCategory.LANGUAGE, 2L);
        assertThat(result).containsEntry(SkillCategory.FRAMEWORK, 1L);
        assertThat(result).doesNotContainKey(SkillCategory.TOOL);
    }

    @Test
    void countGroupedByCategoryReturnsEmptyListWhenNoSkills() {
        List<Object[]> rows = skillRepository.countGroupedByCategory();
        assertThat(rows).isEmpty();
    }

    private Skill save(String name, SkillCategory category) {
        Skill s = new Skill();
        s.setName(name);
        s.setCategory(category);
        return skillRepository.save(s);
    }
}
