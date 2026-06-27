package com.luccadumas.projecthub.controller;

import com.luccadumas.projecthub.domain.enums.ProjectStatus;
import com.luccadumas.projecthub.domain.enums.RiskLevel;
import com.luccadumas.projecthub.dto.request.ProjectCreateRequest;
import com.luccadumas.projecthub.dto.request.ProjectMemberAllocationRequest;
import com.luccadumas.projecthub.dto.request.ProjectStatusUpdateRequest;
import com.luccadumas.projecthub.dto.request.ProjectUpdateRequest;
import com.luccadumas.projecthub.dto.response.ProjectResponse;
import com.luccadumas.projecthub.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@Tag(name = "Projects", description = "Project portfolio management")
@SecurityRequirement(name = "bearerAuth")
public class ProjectController {

    private final ProjectService projectService;

    @GetMapping
    @Operation(summary = "List projects with pagination and filters")
    @ApiResponse(responseCode = "200", description = "Projects retrieved successfully")
    public Page<ProjectResponse> list(
            @Parameter(description = "Filter by project name") @RequestParam(required = false) String name,
            @Parameter(description = "Filter by status") @RequestParam(required = false) ProjectStatus status,
            @Parameter(description = "Filter by calculated risk level") @RequestParam(required = false) RiskLevel risk,
            @Parameter(description = "Filter by manager id") @RequestParam(required = false) Long managerId,
            @Parameter(description = "Filter by start date from") @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDateFrom,
            @Parameter(description = "Filter by start date to") @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDateTo,
            @Parameter(description = "Filter by expected end date from") @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate expectedEndDateFrom,
            @Parameter(description = "Filter by expected end date to") @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate expectedEndDateTo,
            Pageable pageable) {

        return projectService.findAll(
                name, status, risk, managerId,
                startDateFrom, startDateTo, expectedEndDateFrom, expectedEndDateTo, pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get project by id")
    public ProjectResponse getById(@PathVariable Long id) {
        return projectService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new project")
    public ProjectResponse create(@Valid @RequestBody ProjectCreateRequest request) {
        return projectService.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update an existing project")
    public ProjectResponse update(@PathVariable Long id, @Valid @RequestBody ProjectUpdateRequest request) {
        return projectService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a project")
    public void delete(@PathVariable Long id) {
        projectService.delete(id);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update project status following workflow rules")
    public ProjectResponse updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody ProjectStatusUpdateRequest request) {
        return projectService.updateStatus(id, request);
    }

    @PutMapping("/{id}/members")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Allocate members to a project")
    public ProjectResponse allocateMembers(
            @PathVariable Long id,
            @Valid @RequestBody ProjectMemberAllocationRequest request) {
        return projectService.allocateMembers(id, request);
    }
}
