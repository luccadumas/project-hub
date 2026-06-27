package com.luccadumas.projecthub.repository.specification;

import com.luccadumas.projecthub.domain.enums.ProjectStatus;
import com.luccadumas.projecthub.domain.enums.RiskLevel;
import com.luccadumas.projecthub.entity.Project;
import com.luccadumas.projecthub.repository.ProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
class ProjectSpecificationsIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("project_hub")
            .withUsername("project_hub")
            .withPassword("project_hub");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("project-hub.jwt.secret", () -> "test-secret-key-at-least-32-characters-long");
    }

    @Autowired
    private ProjectRepository projectRepository;

    @BeforeEach
    void cleanProjects() {
        projectRepository.deleteAll();
    }

    @Test
    @DisplayName("Should filter projects by persisted risk level")
    void shouldFilterProjectsByPersistedRiskLevel() {
        projectRepository.save(buildProject("Alto Risco", RiskLevel.ALTO));
        projectRepository.save(buildProject("Baixo Risco", RiskLevel.BAIXO));

        Specification<Project> specification = ProjectSpecifications.withFilters(
                null, null, RiskLevel.ALTO, null, null, null, null, null);

        List<Project> results = projectRepository.findAll(specification);

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().getName()).isEqualTo("Alto Risco");
    }

    @Test
    @DisplayName("Should filter projects by partial name")
    void shouldFilterProjectsByPartialName() {
        projectRepository.save(buildProject("Portal Corporativo", RiskLevel.MEDIO));
        projectRepository.save(buildProject("App Mobile", RiskLevel.BAIXO));

        Specification<Project> specification = ProjectSpecifications.withFilters(
                "portal", null, null, null, null, null, null, null);

        List<Project> results = projectRepository.findAll(specification);

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().getName()).isEqualTo("Portal Corporativo");
    }

    private Project buildProject(String name, RiskLevel riskLevel) {
        return Project.builder()
                .name(name)
                .startDate(LocalDate.of(2025, 1, 1))
                .expectedEndDate(LocalDate.of(2025, 6, 1))
                .totalBudget(new BigDecimal("120000"))
                .managerId(1L)
                .status(ProjectStatus.PLANEJADO)
                .riskLevel(riskLevel)
                .build();
    }
}
