package com.luccadumas.projecthub.service;

import com.luccadumas.projecthub.domain.enums.MemberRole;
import com.luccadumas.projecthub.domain.enums.ProjectStatus;
import com.luccadumas.projecthub.domain.enums.RiskLevel;
import com.luccadumas.projecthub.domain.project.ProjectLifecycleValidator;
import com.luccadumas.projecthub.domain.risk.RiskClassificationStrategy;
import com.luccadumas.projecthub.domain.status.ProjectStatusWorkflow;
import com.luccadumas.projecthub.dto.request.ProjectCreateRequest;
import com.luccadumas.projecthub.dto.request.ProjectMemberAllocationRequest;
import com.luccadumas.projecthub.dto.request.ProjectStatusUpdateRequest;
import com.luccadumas.projecthub.dto.request.ProjectUpdateRequest;
import com.luccadumas.projecthub.dto.response.MemberResponse;
import com.luccadumas.projecthub.dto.response.ProjectResponse;
import com.luccadumas.projecthub.entity.Member;
import com.luccadumas.projecthub.entity.Project;
import com.luccadumas.projecthub.exception.BusinessException;
import com.luccadumas.projecthub.exception.ResourceNotFoundException;
import com.luccadumas.projecthub.mapper.ProjectMapper;
import com.luccadumas.projecthub.repository.ProjectRepository;
import com.luccadumas.projecthub.repository.specification.ProjectSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMapper projectMapper;
    private final MemberDirectory memberDirectory;
    private final MemberAllocationService memberAllocationService;
    private final ProjectStatusWorkflow statusWorkflow;
    private final ProjectLifecycleValidator lifecycleValidator;
    private final RiskClassificationStrategy riskClassifier;

    public Page<ProjectResponse> findAll(
            String name,
            ProjectStatus status,
            RiskLevel riskLevel,
            Long managerId,
            LocalDate startDateFrom,
            LocalDate startDateTo,
            LocalDate expectedEndDateFrom,
            LocalDate expectedEndDateTo,
            Pageable pageable) {

        Specification<Project> specification = ProjectSpecifications.withFilters(
                name, status, riskLevel, managerId,
                startDateFrom, startDateTo, expectedEndDateFrom, expectedEndDateTo);

        Page<Project> projects = projectRepository.findAll(specification, pageable);
        Set<Long> managerIds = projects.getContent().stream()
                .map(Project::getManagerId)
                .collect(Collectors.toSet());
        Map<Long, String> managerNames = memberDirectory.resolveManagerNames(managerIds);

        return projects.map(project -> toResponse(project, managerNames));
    }

    public ProjectResponse findById(Long id) {
        Project project = getProject(id);
        return toResponseWithManager(project);
    }

    @Transactional
    public ProjectResponse create(ProjectCreateRequest request) {
        validateDates(request.getStartDate(), request.getExpectedEndDate());
        validateManager(request.getManagerId());

        Set<Member> members = memberAllocationService.resolveMembers(request.getMemberIds(), null);

        Project project = Project.builder()
                .name(request.getName())
                .startDate(request.getStartDate())
                .expectedEndDate(request.getExpectedEndDate())
                .totalBudget(request.getTotalBudget())
                .description(request.getDescription())
                .managerId(request.getManagerId())
                .status(ProjectStatus.EM_ANALISE)
                .members(members)
                .build();
        refreshRiskLevel(project);

        return toResponseWithManager(projectRepository.save(project));
    }

    @Transactional
    public ProjectResponse update(Long id, ProjectUpdateRequest request) {
        Project project = getProject(id);
        lifecycleValidator.ensureModifiable(project.getStatus());

        validateDates(request.getStartDate(), request.getExpectedEndDate());
        validateManager(request.getManagerId());

        Set<Member> members = memberAllocationService.resolveMembers(request.getMemberIds(), id);

        project.setName(request.getName());
        project.setStartDate(request.getStartDate());
        project.setExpectedEndDate(request.getExpectedEndDate());
        project.setActualEndDate(request.getActualEndDate());
        project.setTotalBudget(request.getTotalBudget());
        project.setDescription(request.getDescription());
        project.setManagerId(request.getManagerId());
        project.getMembers().clear();
        project.getMembers().addAll(members);
        refreshRiskLevel(project);

        return toResponseWithManager(projectRepository.save(project));
    }

    @Transactional
    public void delete(Long id) {
        Project project = getProject(id);

        if (project.getStatus().blocksDeletion()) {
            throw new BusinessException(
                    "Project cannot be deleted while status is " + project.getStatus());
        }

        projectRepository.delete(project);
    }

    @Transactional
    public ProjectResponse updateStatus(Long id, ProjectStatusUpdateRequest request) {
        Project project = getProject(id);
        statusWorkflow.validateTransition(project.getStatus(), request.getStatus());

        if (request.getStatus() == ProjectStatus.ENCERRADO && project.getActualEndDate() == null) {
            project.setActualEndDate(LocalDate.now());
        }

        project.setStatus(request.getStatus());
        return toResponseWithManager(projectRepository.save(project));
    }

    @Transactional
    public ProjectResponse allocateMembers(Long id, ProjectMemberAllocationRequest request) {
        Project project = getProject(id);
        lifecycleValidator.ensureModifiable(project.getStatus());

        Set<Member> members = memberAllocationService.resolveMembers(request.getMemberIds(), id);

        project.getMembers().clear();
        project.getMembers().addAll(members);

        return toResponseWithManager(projectRepository.save(project));
    }

    private Project getProject(Long id) {
        return projectRepository.findWithMembersById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));
    }

    private void validateManager(Long managerId) {
        MemberResponse manager = memberDirectory.getRequired(managerId);
        MemberRole role = MemberRole.parseRole(manager.getRole());
        lifecycleValidator.validateManager(role, managerId);
    }

    private void validateDates(LocalDate startDate, LocalDate expectedEndDate) {
        if (expectedEndDate.isBefore(startDate)) {
            throw new BusinessException("Expected end date must be on or after start date");
        }
    }

    private void refreshRiskLevel(Project project) {
        project.setRiskLevel(riskClassifier.classify(
                project.getTotalBudget(),
                project.getStartDate(),
                project.getExpectedEndDate()));
    }

    private ProjectResponse toResponseWithManager(Project project) {
        String managerName = memberDirectory.getRequired(project.getManagerId()).getName();
        return projectMapper.toResponse(project, managerName);
    }

    private ProjectResponse toResponse(Project project, Map<Long, String> managerNames) {
        String managerName = managerNames.getOrDefault(project.getManagerId(), "Unknown");
        return projectMapper.toResponse(project, managerName);
    }
}
