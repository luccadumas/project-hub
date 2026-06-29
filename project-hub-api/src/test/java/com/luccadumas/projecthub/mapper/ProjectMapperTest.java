package com.luccadumas.projecthub.mapper;

import com.luccadumas.projecthub.domain.enums.MemberRole;
import com.luccadumas.projecthub.domain.enums.ProjectStatus;
import com.luccadumas.projecthub.domain.enums.RiskLevel;
import com.luccadumas.projecthub.dto.response.ProjectResponse;
import com.luccadumas.projecthub.entity.Member;
import com.luccadumas.projecthub.entity.Project;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ProjectMapperTest {

    private final ProjectMapper mapper = new ProjectMapper();

    @Test
    @DisplayName("Should map project entity to response with manager name and members")
    void shouldMapProjectToResponse() {
        Member employee = Member.builder()
                .id(2L)
                .name("Bruno Costa")
                .role(MemberRole.EMPLOYEE)
                .build();

        Project project = Project.builder()
                .id(1L)
                .name("Portal Interno")
                .startDate(LocalDate.of(2025, 1, 10))
                .expectedEndDate(LocalDate.of(2025, 4, 10))
                .totalBudget(new BigDecimal("85000"))
                .description("Modernização")
                .managerId(1L)
                .status(ProjectStatus.IN_PROGRESS)
                .riskLevel(RiskLevel.LOW)
                .members(Set.of(employee))
                .build();

        ProjectResponse response = mapper.toResponse(project, "Ana Silva");

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getManagerName()).isEqualTo("Ana Silva");
        assertThat(response.getMembers()).hasSize(1);
        assertThat(response.getMembers().iterator().next().getRole()).isEqualTo("employee");
    }

    @Test
    @DisplayName("Should map project without members to empty member set")
    void shouldMapProjectWithoutMembers() {
        Project project = Project.builder()
                .id(2L)
                .name("Migração Cloud")
                .startDate(LocalDate.of(2025, 3, 1))
                .expectedEndDate(LocalDate.of(2025, 9, 1))
                .totalBudget(new BigDecimal("320000"))
                .managerId(1L)
                .status(ProjectStatus.PLANNED)
                .riskLevel(RiskLevel.MEDIUM)
                .build();

        ProjectResponse response = mapper.toResponse(project);

        assertThat(response.getManagerName()).isNull();
        assertThat(response.getMembers()).isEmpty();
    }
}
