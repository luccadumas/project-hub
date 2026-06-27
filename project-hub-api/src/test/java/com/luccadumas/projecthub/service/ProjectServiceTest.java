package com.luccadumas.projecthub.service;

import com.luccadumas.projecthub.domain.enums.MemberRole;
import com.luccadumas.projecthub.domain.enums.ProjectStatus;
import com.luccadumas.projecthub.domain.enums.RiskLevel;
import com.luccadumas.projecthub.domain.project.ProjectLifecycleValidator;
import com.luccadumas.projecthub.domain.risk.RiskClassificationStrategy;
import com.luccadumas.projecthub.domain.status.ProjectStatusWorkflow;
import com.luccadumas.projecthub.dto.request.*;
import com.luccadumas.projecthub.dto.response.MemberResponse;
import com.luccadumas.projecthub.dto.response.ProjectResponse;
import com.luccadumas.projecthub.entity.Member;
import com.luccadumas.projecthub.entity.Project;
import com.luccadumas.projecthub.exception.BusinessException;
import com.luccadumas.projecthub.exception.ResourceNotFoundException;
import com.luccadumas.projecthub.mapper.ProjectMapper;
import com.luccadumas.projecthub.repository.ProjectRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectMapper projectMapper;

    @Mock
    private MemberDirectory memberDirectory;

    @Mock
    private MemberAllocationService memberAllocationService;

    @Mock
    private ProjectStatusWorkflow statusWorkflow;

    @Mock
    private ProjectLifecycleValidator lifecycleValidator;

    @Mock
    private RiskClassificationStrategy riskClassifier;

    @InjectMocks
    private ProjectService projectService;

    @Test
    @DisplayName("Should find project by id")
    void shouldFindProjectById() {
        Project project = sampleProject(1L, ProjectStatus.PLANEJADO);
        ProjectResponse response = ProjectResponse.builder().id(1L).name("Portal").build();

        when(projectRepository.findWithMembersById(1L)).thenReturn(Optional.of(project));
        when(memberDirectory.getRequired(1L)).thenReturn(managerResponse());
        when(projectMapper.toResponse(project, "Ana Silva")).thenReturn(response);

        ProjectResponse result = projectService.findById(1L);

        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Should throw when project is not found")
    void shouldThrowWhenProjectNotFound() {
        when(projectRepository.findWithMembersById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Should list projects with pagination")
    void shouldListProjectsWithPagination() {
        Project project = sampleProject(1L, ProjectStatus.EM_ANALISE);
        PageRequest pageable = PageRequest.of(0, 10);

        when(projectRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(project)));
        when(memberDirectory.resolveManagerNames(Set.of(1L)))
                .thenReturn(Map.of(1L, "Ana Silva"));
        when(projectMapper.toResponse(project, "Ana Silva"))
                .thenReturn(ProjectResponse.builder().id(1L).build());

        var page = projectService.findAll(null, null, null, null, null, null, null, null, pageable);

        assertThat(page.getTotalElements()).isEqualTo(1);
        verify(memberDirectory).resolveManagerNames(Set.of(1L));
    }

    @ParameterizedTest
    @EnumSource(value = ProjectStatus.class, names = {"INICIADO", "EM_ANDAMENTO", "ENCERRADO"})
    @DisplayName("Should block project deletion for protected statuses")
    void shouldBlockProjectDeletionForProtectedStatuses(ProjectStatus status) {
        Project project = Project.builder().id(1L).status(status).build();
        when(projectRepository.findWithMembersById(1L)).thenReturn(Optional.of(project));

        assertThatThrownBy(() -> projectService.delete(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("cannot be deleted");
    }

    @Test
    @DisplayName("Should delete project when status allows deletion")
    void shouldDeleteProjectWhenAllowed() {
        Project project = Project.builder().id(1L).status(ProjectStatus.EM_ANALISE).build();
        when(projectRepository.findWithMembersById(1L)).thenReturn(Optional.of(project));

        projectService.delete(1L);

        verify(projectRepository).delete(project);
    }

    @Test
    @DisplayName("Should validate status workflow on update")
    void shouldValidateStatusWorkflowOnUpdate() {
        Project project = Project.builder().id(1L).status(ProjectStatus.EM_ANALISE).build();
        ProjectStatusUpdateRequest request = new ProjectStatusUpdateRequest();
        request.setStatus(ProjectStatus.INICIADO);

        when(projectRepository.findWithMembersById(1L)).thenReturn(Optional.of(project));
        doThrow(new BusinessException("Invalid status transition"))
                .when(statusWorkflow).validateTransition(ProjectStatus.EM_ANALISE, ProjectStatus.INICIADO);

        assertThatThrownBy(() -> projectService.updateStatus(1L, request))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("Should set actual end date when closing project")
    void shouldSetActualEndDateWhenClosingProject() {
        Project project = sampleProject(1L, ProjectStatus.EM_ANDAMENTO);
        ProjectStatusUpdateRequest request = new ProjectStatusUpdateRequest();
        request.setStatus(ProjectStatus.ENCERRADO);

        when(projectRepository.findWithMembersById(1L)).thenReturn(Optional.of(project));
        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(memberDirectory.getRequired(1L)).thenReturn(managerResponse());
        when(projectMapper.toResponse(any(Project.class), eq("Ana Silva")))
                .thenReturn(ProjectResponse.builder().id(1L).build());

        projectService.updateStatus(1L, request);

        verify(projectRepository).save(argThat(saved ->
                saved.getStatus() == ProjectStatus.ENCERRADO && saved.getActualEndDate() != null));
    }

    @Test
    @DisplayName("Should reject invalid date range")
    void shouldRejectInvalidDateRange() {
        ProjectCreateRequest request = new ProjectCreateRequest();
        request.setStartDate(LocalDate.of(2025, 6, 1));
        request.setExpectedEndDate(LocalDate.of(2025, 1, 1));

        assertThatThrownBy(() -> projectService.create(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Expected end date");
    }

    @Test
    @DisplayName("Should create project with initial status em analise")
    void shouldCreateProjectWithInitialStatus() {
        ProjectCreateRequest request = buildCreateRequest();

        when(memberDirectory.getRequired(1L)).thenReturn(managerResponse());
        when(memberAllocationService.resolveMembers(Set.of(2L), null)).thenReturn(Set.of(employee()));
        when(riskClassifier.classify(any(), any(), any())).thenReturn(RiskLevel.BAIXO);
        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> {
            Project saved = invocation.getArgument(0);
            saved.setId(99L);
            return saved;
        });
        when(projectMapper.toResponse(any(Project.class), eq("Ana Silva")))
                .thenReturn(ProjectResponse.builder().id(99L).build());

        projectService.create(request);

        verify(projectRepository).save(argThat(project ->
                project.getStatus() == ProjectStatus.EM_ANALISE
                        && project.getName().equals("New Project")
                        && project.getRiskLevel() == RiskLevel.BAIXO));
    }

    @Test
    @DisplayName("Should refresh persisted risk level when project data changes")
    void shouldRefreshPersistedRiskLevelOnUpdate() {
        Project project = sampleProject(1L, ProjectStatus.PLANEJADO);
        ProjectUpdateRequest request = new ProjectUpdateRequest();
        request.setName("Updated");
        request.setStartDate(LocalDate.of(2025, 1, 1));
        request.setExpectedEndDate(LocalDate.of(2025, 12, 1));
        request.setTotalBudget(new BigDecimal("600000"));
        request.setManagerId(1L);
        request.setMemberIds(Set.of(2L));

        when(projectRepository.findWithMembersById(1L)).thenReturn(Optional.of(project));
        when(memberDirectory.getRequired(1L)).thenReturn(managerResponse());
        when(memberAllocationService.resolveMembers(Set.of(2L), 1L)).thenReturn(Set.of(employee()));
        when(riskClassifier.classify(any(), any(), any())).thenReturn(RiskLevel.ALTO);
        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(projectMapper.toResponse(any(Project.class), eq("Ana Silva")))
                .thenReturn(ProjectResponse.builder().id(1L).build());

        projectService.update(1L, request);

        verify(riskClassifier).classify(
                new BigDecimal("600000"),
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 12, 1));
        verify(projectRepository).save(argThat(saved -> saved.getRiskLevel() == RiskLevel.ALTO));
    }

    @Test
    @DisplayName("Should update project and reallocate members")
    void shouldUpdateProjectAndReallocateMembers() {
        Project project = sampleProject(1L, ProjectStatus.PLANEJADO);
        project.getMembers().add(employee());

        ProjectUpdateRequest request = new ProjectUpdateRequest();
        request.setName("Updated");
        request.setStartDate(LocalDate.of(2025, 1, 1));
        request.setExpectedEndDate(LocalDate.of(2025, 6, 1));
        request.setTotalBudget(new BigDecimal("120000"));
        request.setManagerId(1L);
        request.setMemberIds(Set.of(2L, 3L));

        when(projectRepository.findWithMembersById(1L)).thenReturn(Optional.of(project));
        when(memberDirectory.getRequired(1L)).thenReturn(managerResponse());
        when(memberAllocationService.resolveMembers(Set.of(2L, 3L), 1L)).thenReturn(Set.of(employee()));
        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(projectMapper.toResponse(any(Project.class), eq("Ana Silva")))
                .thenReturn(ProjectResponse.builder().id(1L).name("Updated").build());

        ProjectResponse response = projectService.update(1L, request);

        assertThat(response.getName()).isEqualTo("Updated");
        verify(projectRepository).save(argThat(saved -> saved.getName().equals("Updated")));
    }

    @Test
    @DisplayName("Should allocate members to existing project")
    void shouldAllocateMembersToExistingProject() {
        Project project = sampleProject(1L, ProjectStatus.PLANEJADO);
        ProjectMemberAllocationRequest request = new ProjectMemberAllocationRequest();
        request.setMemberIds(Set.of(2L));

        when(projectRepository.findWithMembersById(1L)).thenReturn(Optional.of(project));
        when(memberAllocationService.resolveMembers(Set.of(2L), 1L)).thenReturn(Set.of(employee()));
        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(memberDirectory.getRequired(1L)).thenReturn(managerResponse());
        when(projectMapper.toResponse(any(Project.class), eq("Ana Silva")))
                .thenReturn(ProjectResponse.builder().id(1L).build());

        projectService.allocateMembers(1L, request);

        verify(memberAllocationService).resolveMembers(Set.of(2L), 1L);
        verify(projectRepository).save(argThat(saved -> saved.getMembers().size() == 1));
    }

    @Test
    @DisplayName("Should reject manager that is not a gerente")
    void shouldRejectNonManagerRole() {
        ProjectCreateRequest request = buildCreateRequest();
        request.setManagerId(2L);

        when(memberDirectory.getRequired(2L)).thenReturn(
                MemberResponse.builder().id(2L).name("Bruno").role("funcionario").build());
        doThrow(new BusinessException("Only members with role 'gerente' can be assigned as project manager. Member id: 2"))
                .when(lifecycleValidator).validateManager(any(MemberRole.class), eq(2L));

        assertThatThrownBy(() -> projectService.create(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("gerente");
    }

    @Test
    @DisplayName("Should block update for terminal project status")
    void shouldBlockUpdateForTerminalStatus() {
        Project project = sampleProject(1L, ProjectStatus.ENCERRADO);
        ProjectUpdateRequest request = new ProjectUpdateRequest();
        request.setName("Updated");
        request.setStartDate(LocalDate.of(2025, 1, 1));
        request.setExpectedEndDate(LocalDate.of(2025, 6, 1));
        request.setTotalBudget(new BigDecimal("120000"));
        request.setManagerId(1L);
        request.setMemberIds(Set.of(2L));

        when(projectRepository.findWithMembersById(1L)).thenReturn(Optional.of(project));
        doThrow(new BusinessException("Project cannot be modified while status is ENCERRADO"))
                .when(lifecycleValidator).ensureModifiable(ProjectStatus.ENCERRADO);

        assertThatThrownBy(() -> projectService.update(1L, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("cannot be modified");
    }

    @Test
    @DisplayName("Should block member allocation for terminal project status")
    void shouldBlockAllocationForTerminalStatus() {
        Project project = sampleProject(1L, ProjectStatus.CANCELADO);
        ProjectMemberAllocationRequest request = new ProjectMemberAllocationRequest();
        request.setMemberIds(Set.of(2L));

        when(projectRepository.findWithMembersById(1L)).thenReturn(Optional.of(project));
        doThrow(new BusinessException("Project cannot be modified while status is CANCELADO"))
                .when(lifecycleValidator).ensureModifiable(ProjectStatus.CANCELADO);

        assertThatThrownBy(() -> projectService.allocateMembers(1L, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("cannot be modified");
    }

    private ProjectCreateRequest buildCreateRequest() {
        ProjectCreateRequest request = new ProjectCreateRequest();
        request.setName("New Project");
        request.setStartDate(LocalDate.of(2025, 1, 1));
        request.setExpectedEndDate(LocalDate.of(2025, 4, 1));
        request.setTotalBudget(new BigDecimal("50000"));
        request.setManagerId(1L);
        request.setMemberIds(Set.of(2L));
        return request;
    }

    private Project sampleProject(Long id, ProjectStatus status) {
        return Project.builder()
                .id(id)
                .name("Portal")
                .startDate(LocalDate.of(2025, 1, 1))
                .expectedEndDate(LocalDate.of(2025, 4, 1))
                .totalBudget(new BigDecimal("50000"))
                .managerId(1L)
                .status(status)
                .riskLevel(RiskLevel.BAIXO)
                .build();
    }

    private Member employee() {
        return Member.builder().id(2L).name("Bruno").role(MemberRole.FUNCIONARIO).build();
    }

    private MemberResponse managerResponse() {
        return MemberResponse.builder().id(1L).name("Ana Silva").role("gerente").build();
    }
}
