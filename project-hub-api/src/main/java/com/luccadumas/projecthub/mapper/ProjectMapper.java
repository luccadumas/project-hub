package com.luccadumas.projecthub.mapper;

import com.luccadumas.projecthub.dto.response.MemberSummaryResponse;
import com.luccadumas.projecthub.dto.response.ProjectResponse;
import com.luccadumas.projecthub.entity.Member;
import com.luccadumas.projecthub.entity.Project;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ProjectMapper {

    public ProjectResponse toResponse(Project project) {
        return toResponse(project, null);
    }

    public ProjectResponse toResponse(Project project, String managerName) {
        return ProjectResponse.builder()
                .id(project.getId())
                .name(project.getName())
                .startDate(project.getStartDate())
                .expectedEndDate(project.getExpectedEndDate())
                .actualEndDate(project.getActualEndDate())
                .totalBudget(project.getTotalBudget())
                .description(project.getDescription())
                .managerId(project.getManagerId())
                .managerName(managerName)
                .status(project.getStatus())
                .riskLevel(project.getRiskLevel())
                .members(toMemberSummaries(project.getMembers()))
                .build();
    }

    private Set<MemberSummaryResponse> toMemberSummaries(Set<Member> members) {
        if (members == null || members.isEmpty()) {
            return Set.of();
        }
        return members.stream()
                .map(member -> MemberSummaryResponse.builder()
                        .id(member.getId())
                        .name(member.getName())
                        .role(member.getRole().name().toLowerCase())
                        .build())
                .collect(Collectors.toSet());
    }
}
