package com.careeros.user.repository;

import com.careeros.audit.ApplicationAuditorAware;
import com.careeros.config.JpaAuditingConfig;
import com.careeros.user.AbstractPostgresContainerTest;
import com.careeros.user.entity.Skill;
import com.careeros.user.entity.SkillCategory;
import com.careeros.user.entity.SkillProficiency;
import com.careeros.user.entity.UserSkill;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Import({JpaAuditingConfig.class, ApplicationAuditorAware.class})
@TestPropertySource(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class UserSkillRepositoryTest extends AbstractPostgresContainerTest {

    @Autowired private UserSkillRepository userSkillRepository;
    @Autowired private SkillRepository skillRepository;
    @Autowired private TestEntityManager entityManager;

    @Test
    void entityGraphFetchesSkillInOneQuery() {
        UUID userId = UUID.randomUUID();
        Skill skill = new Skill();
        skill.setName("Java");
        skill.setCategory(SkillCategory.LANGUAGE);
        skillRepository.save(skill);

        UserSkill us = new UserSkill();
        us.setUserId(userId);
        us.setSkill(skill);
        us.setProficiency(SkillProficiency.INTERMEDIATE);
        us.setYearsOfExperience(BigDecimal.valueOf(3.5));
        userSkillRepository.save(us);
        entityManager.flush();
        entityManager.clear();

        List<UserSkill> found = userSkillRepository.findAllByUserId(userId);
        assertThat(found).hasSize(1);
        assertThat(found.get(0).getSkill().getName()).isEqualTo("Java");
    }

    @Test
    void countByProficiencyGroupsCorrectly() {
        UUID userId = UUID.randomUUID();
        Skill s1 = createSkill("Python");
        Skill s2 = createSkill("Go");
        assignSkill(userId, s1, SkillProficiency.BEGINNER);
        assignSkill(userId, s2, SkillProficiency.EXPERT);
        entityManager.flush();

        List<Object[]> counts = userSkillRepository.countByProficiencyForUser(userId);
        assertThat(counts).hasSize(2);
    }

    private Skill createSkill(String name) {
        Skill s = new Skill();
        s.setName(name);
        s.setCategory(SkillCategory.LANGUAGE);
        return skillRepository.save(s);
    }

    private void assignSkill(UUID userId, Skill skill, SkillProficiency prof) {
        UserSkill us = new UserSkill();
        us.setUserId(userId);
        us.setSkill(skill);
        us.setProficiency(prof);
        userSkillRepository.save(us);
    }
}
