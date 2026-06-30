package com.careeros.user.repository;

import com.careeros.user.AbstractPostgresContainerTest;
import com.careeros.user.entity.Roadmap;
import com.careeros.user.entity.RoadmapStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class RoadmapRepositoryTest extends AbstractPostgresContainerTest {

    @Autowired
    private RoadmapRepository repository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void findAllByUserIdReturnsOnlyUserRoadmaps() {
        UUID userId = UUID.randomUUID();
        saveRoadmap(userId, "My Roadmap", RoadmapStatus.ACTIVE, false);
        saveRoadmap(userId, "Another Roadmap", RoadmapStatus.DRAFT, false);
        saveRoadmap(UUID.randomUUID(), "Other User Roadmap", RoadmapStatus.ACTIVE, false);

        Page<Roadmap> page = repository.findAllByUserId(userId, PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getContent()).extracting("title")
                .containsExactlyInAnyOrder("My Roadmap", "Another Roadmap");
    }

    @Test
    void findByIdAndUserIdReturnsEmptyForWrongUser() {
        UUID ownerId = UUID.randomUUID();
        Roadmap r = saveRoadmap(ownerId, "Title", RoadmapStatus.ACTIVE, false);

        assertThat(repository.findByIdAndUserId(r.getId(), UUID.randomUUID())).isEmpty();
        assertThat(repository.findByIdAndUserId(r.getId(), ownerId)).isPresent();
    }

    @Test
    void findAllByIsTemplateTrueReturnsTemplatesOnly() {
        saveRoadmap(UUID.randomUUID(), "Template A", RoadmapStatus.ACTIVE, true);
        saveRoadmap(UUID.randomUUID(), "Template B", RoadmapStatus.ACTIVE, true);
        saveRoadmap(UUID.randomUUID(), "Not Template", RoadmapStatus.ACTIVE, false);

        Page<Roadmap> page = repository.findAllByIsTemplateTrue(PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getContent()).extracting("title")
                .containsExactlyInAnyOrder("Template A", "Template B");
    }

    @Test
    void countByUserIdAndStatusWorksCorrectly() {
        UUID userId = UUID.randomUUID();
        saveRoadmap(userId, "R1", RoadmapStatus.COMPLETED, false);
        saveRoadmap(userId, "R2", RoadmapStatus.COMPLETED, false);
        saveRoadmap(userId, "R3", RoadmapStatus.ACTIVE, false);

        long completedCount = repository.countByUserIdAndStatus(userId, RoadmapStatus.COMPLETED);
        long activeCount = repository.countByUserIdAndStatus(userId, RoadmapStatus.ACTIVE);

        assertThat(completedCount).isEqualTo(2);
        assertThat(activeCount).isEqualTo(1);
    }

    private Roadmap saveRoadmap(UUID userId, String title, RoadmapStatus status, boolean isTemplate) {
        Roadmap r = new Roadmap();
        r.setUserId(userId);
        r.setTitle(title);
        r.setStatus(status);
        r.setIsTemplate(isTemplate);
        entityManager.persistAndFlush(r);
        entityManager.clear();
        return r;
    }
}
