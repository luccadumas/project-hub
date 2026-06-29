package com.luccadumas.projecthub;

import com.luccadumas.projecthub.domain.enums.ProjectStatus;
import com.luccadumas.projecthub.dto.request.ProjectCreateRequest;
import com.luccadumas.projecthub.dto.request.ProjectStatusUpdateRequest;
import com.luccadumas.projecthub.dto.request.ProjectUpdateRequest;
import com.luccadumas.projecthub.dto.response.MemberResponse;
import com.luccadumas.projecthub.dto.response.ProjectResponse;
import com.luccadumas.projecthub.exception.BusinessException;
import com.luccadumas.projecthub.service.ExternalMemberService;
import com.luccadumas.projecthub.service.ProjectService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Testcontainers
class BusinessRulesIntegrationTest {

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
    private ExternalMemberService externalMemberService;

    @Autowired
    private ProjectService projectService;

    @Test
    @DisplayName("Should persist member through external API flow")
    void shouldPersistMemberThroughExternalApiFlow() {
        com.luccadumas.projecthub.dto.request.MemberCreateRequest request =
                new com.luccadumas.projecthub.dto.request.MemberCreateRequest();
        request.setName("Integracao Teste");
        request.setRole("employee");

        MemberResponse created = externalMemberService.create(request);

        assertThat(created.getId()).isNotNull();
        assertThat(created.getRole()).isEqualTo("employee");
    }

    @Test
    @DisplayName("Should enforce project lifecycle and manager rules end to end")
    void shouldEnforceProjectLifecycleAndManagerRulesEndToEnd() {
        ProjectCreateRequest createRequest = new ProjectCreateRequest();
        createRequest.setName("Integracao E2E");
        createRequest.setStartDate(LocalDate.of(2025, 1, 1));
        createRequest.setExpectedEndDate(LocalDate.of(2025, 4, 1));
        createRequest.setTotalBudget(new BigDecimal("75000"));
        createRequest.setManagerId(1L);
        createRequest.setMemberIds(Set.of(2L));

        ProjectResponse created = projectService.create(createRequest);

        assertThat(created.getStatus()).isEqualTo(ProjectStatus.UNDER_ANALYSIS);
        assertThat(created.getRiskLevel()).isNotNull();

        ProjectCreateRequest invalidManagerRequest = new ProjectCreateRequest();
        invalidManagerRequest.setName("Gerente inválido");
        invalidManagerRequest.setStartDate(LocalDate.of(2025, 1, 1));
        invalidManagerRequest.setExpectedEndDate(LocalDate.of(2025, 4, 1));
        invalidManagerRequest.setTotalBudget(new BigDecimal("75000"));
        invalidManagerRequest.setManagerId(2L);
        invalidManagerRequest.setMemberIds(Set.of(3L));

        assertThatThrownBy(() -> projectService.create(invalidManagerRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("manager");

        advanceToStatus(created.getId(), ProjectStatus.IN_PROGRESS);

        assertThatThrownBy(() -> projectService.delete(created.getId()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("cannot be deleted");

        advanceToStatus(created.getId(), ProjectStatus.COMPLETED);

        ProjectUpdateRequest updateRequest = new ProjectUpdateRequest();
        updateRequest.setName("Não deve atualizar");
        updateRequest.setStartDate(LocalDate.of(2025, 1, 1));
        updateRequest.setExpectedEndDate(LocalDate.of(2025, 4, 1));
        updateRequest.setTotalBudget(new BigDecimal("75000"));
        updateRequest.setManagerId(1L);
        updateRequest.setMemberIds(Set.of(2L));

        assertThatThrownBy(() -> projectService.update(created.getId(), updateRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("cannot be modified");

        ProjectStatusUpdateRequest cancelRequest = new ProjectStatusUpdateRequest();
        cancelRequest.setStatus(ProjectStatus.CANCELED);

        assertThatThrownBy(() -> projectService.updateStatus(created.getId(), cancelRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Cannot cancel");

        var filtered = projectService.findAll(
                "Integracao E2E",
                null,
                null,
                null,
                null,
                null,
                LocalDate.of(2025, 4, 1),
                LocalDate.of(2025, 4, 30),
                PageRequest.of(0, 10));

        assertThat(filtered.getTotalElements()).isEqualTo(1);
    }

    private void advanceToStatus(Long projectId, ProjectStatus targetStatus) {
        ProjectStatus[] path = {
                ProjectStatus.UNDER_ANALYSIS,
                ProjectStatus.ANALYSIS_COMPLETED,
                ProjectStatus.ANALYSIS_APPROVED,
                ProjectStatus.STARTED,
                ProjectStatus.PLANNED,
                ProjectStatus.IN_PROGRESS,
                ProjectStatus.COMPLETED
        };

        ProjectResponse current = projectService.findById(projectId);
        int currentIndex = -1;
        int targetIndex = -1;
        for (int i = 0; i < path.length; i++) {
            if (path[i] == current.getStatus()) {
                currentIndex = i;
            }
            if (path[i] == targetStatus) {
                targetIndex = i;
            }
        }

        if (currentIndex == -1 || targetIndex == -1) {
            updateStatus(projectId, targetStatus);
            return;
        }

        for (int i = currentIndex + 1; i <= targetIndex; i++) {
            updateStatus(projectId, path[i]);
        }
    }

    private void updateStatus(Long projectId, ProjectStatus status) {
        ProjectStatusUpdateRequest request = new ProjectStatusUpdateRequest();
        request.setStatus(status);
        projectService.updateStatus(projectId, request);
    }
}
