package com.luccadumas.projecthub.service;

import com.luccadumas.projecthub.domain.enums.MemberRole;
import com.luccadumas.projecthub.domain.enums.ProjectStatus;
import com.luccadumas.projecthub.dto.response.MemberResponse;
import com.luccadumas.projecthub.entity.Member;
import com.luccadumas.projecthub.exception.BusinessException;
import com.luccadumas.projecthub.exception.ResourceNotFoundException;
import com.luccadumas.projecthub.integration.MemberExternalClient;
import com.luccadumas.projecthub.repository.MemberRepository;
import com.luccadumas.projecthub.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberAllocationService {

    private static final int MIN_MEMBERS = 1;
    private static final int MAX_MEMBERS = 10;
    private static final int MAX_ACTIVE_PROJECTS_PER_MEMBER = 3;
    private static final List<ProjectStatus> INACTIVE_STATUSES =
            List.of(ProjectStatus.COMPLETED, ProjectStatus.CANCELED);

    private final MemberExternalClient memberExternalClient;
    private final MemberRepository memberRepository;
    private final ProjectRepository projectRepository;

    public Set<Member> resolveMembers(Set<Long> memberIds, Long currentProjectId) {
        validateMemberCount(memberIds);

        Map<Long, MemberResponse> externalMembers = memberExternalClient.findByIds(memberIds);
        for (Long memberId : memberIds) {
            if (!externalMembers.containsKey(memberId)) {
                throw new ResourceNotFoundException("Member not found with id: " + memberId);
            }
        }

        Map<Long, Long> activeProjectCounts =
                projectRepository.countActiveProjectsByMemberIds(memberIds, INACTIVE_STATUSES);
        Set<Long> memberIdsInCurrentProject = resolveMemberIdsInProject(currentProjectId);

        for (Long memberId : memberIds) {
            MemberResponse externalMember = externalMembers.get(memberId);
            MemberRole role = MemberRole.parseRole(externalMember.getRole());

            if (!role.canBeAllocatedToProject()) {
                throw new BusinessException(
                        "Only members with role 'employee' can be allocated to projects. Member id: " + memberId);
            }

            validateActiveProjectLimit(memberId, activeProjectCounts, memberIdsInCurrentProject);
        }

        Map<Long, Member> persistedMembers = memberRepository.findAllById(memberIds).stream()
                .collect(Collectors.toMap(Member::getId, member -> member));

        Set<Member> members = new HashSet<>();
        for (Long memberId : memberIds) {
            Member member = persistedMembers.get(memberId);
            if (member == null) {
                throw new ResourceNotFoundException("Member not found with id: " + memberId);
            }
            members.add(member);
        }

        return members;
    }

    private void validateMemberCount(Set<Long> memberIds) {
        if (memberIds == null || memberIds.size() < MIN_MEMBERS) {
            throw new BusinessException("Each project must have at least " + MIN_MEMBERS + " allocated member");
        }
        if (memberIds.size() > MAX_MEMBERS) {
            throw new BusinessException("Each project can have at most " + MAX_MEMBERS + " allocated members");
        }
    }

    private Set<Long> resolveMemberIdsInProject(Long currentProjectId) {
        if (currentProjectId == null) {
            return Set.of();
        }

        return projectRepository.findWithMembersById(currentProjectId)
                .map(project -> project.getMembers().stream()
                        .map(Member::getId)
                        .collect(Collectors.toSet()))
                .orElse(Set.of());
    }

    private void validateActiveProjectLimit(
            Long memberId,
            Map<Long, Long> activeProjectCounts,
            Set<Long> memberIdsInCurrentProject) {
        long activeProjects = activeProjectCounts.getOrDefault(memberId, 0L);

        if (memberIdsInCurrentProject.contains(memberId)) {
            activeProjects--;
        }

        if (activeProjects >= MAX_ACTIVE_PROJECTS_PER_MEMBER) {
            throw new BusinessException(
                    "Member id " + memberId + " is already allocated to "
                            + MAX_ACTIVE_PROJECTS_PER_MEMBER + " active projects");
        }
    }
}
