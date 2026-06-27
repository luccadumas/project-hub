package com.luccadumas.projecthub.service;

import com.luccadumas.projecthub.domain.enums.MemberRole;
import com.luccadumas.projecthub.domain.enums.ProjectStatus;
import com.luccadumas.projecthub.dto.response.MemberResponse;
import com.luccadumas.projecthub.entity.Member;
import com.luccadumas.projecthub.entity.Project;
import com.luccadumas.projecthub.exception.BusinessException;
import com.luccadumas.projecthub.integration.MemberExternalClient;
import com.luccadumas.projecthub.repository.MemberRepository;
import com.luccadumas.projecthub.repository.ProjectRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MemberAllocationServiceTest {

    @Mock
    private MemberExternalClient memberExternalClient;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private ProjectRepository projectRepository;

    @InjectMocks
    private MemberAllocationService memberAllocationService;

    @Test
    @DisplayName("Should resolve employee members successfully")
    void shouldResolveEmployeeMembersSuccessfully() {
        when(memberExternalClient.findByIds(Set.of(2L))).thenReturn(Map.of(
                2L, MemberResponse.builder().id(2L).name("Employee").role("funcionario").build()
        ));
        when(projectRepository.countActiveProjectsByMemberIds(eq(Set.of(2L)), any()))
                .thenReturn(Map.of(2L, 1L));
        when(memberRepository.findAllById(Set.of(2L))).thenReturn(List.of(
                Member.builder().id(2L).name("Employee").role(MemberRole.FUNCIONARIO).build()
        ));

        var members = memberAllocationService.resolveMembers(Set.of(2L), null);

        assertThat(members).hasSize(1);
        assertThat(members.iterator().next().getRole()).isEqualTo(MemberRole.FUNCIONARIO);
        verify(memberExternalClient).findByIds(Set.of(2L));
    }

    @Test
    void shouldBlockNonEmployeeMembers() {
        when(memberExternalClient.findByIds(Set.of(1L))).thenReturn(Map.of(
                1L, MemberResponse.builder().id(1L).name("Manager").role("gerente").build()
        ));

        assertThatThrownBy(() -> memberAllocationService.resolveMembers(Set.of(1L), null))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("funcionario");
    }

    @Test
    @DisplayName("Should block when member exceeds active project limit")
    void shouldBlockWhenMemberExceedsActiveProjectLimit() {
        when(memberExternalClient.findByIds(Set.of(2L))).thenReturn(Map.of(
                2L, MemberResponse.builder().id(2L).name("Employee").role("funcionario").build()
        ));
        when(projectRepository.countActiveProjectsByMemberIds(eq(Set.of(2L)), any()))
                .thenReturn(Map.of(2L, 3L));

        assertThatThrownBy(() -> memberAllocationService.resolveMembers(Set.of(2L), null))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("3 active projects");
    }

    @Test
    @DisplayName("Should block when member count is below minimum")
    void shouldBlockWhenMemberCountBelowMinimum() {
        assertThatThrownBy(() -> memberAllocationService.resolveMembers(Set.of(), null))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("at least 1");
    }

    @Test
    @DisplayName("Should block when member count exceeds maximum")
    void shouldBlockWhenMemberCountExceedsMaximum() {
        Set<Long> memberIds = Set.of(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L, 11L);

        assertThatThrownBy(() -> memberAllocationService.resolveMembers(memberIds, null))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("at most 10");
    }

    @Test
    @DisplayName("Should allow reallocation when member is already in current project")
    void shouldAllowReallocationForCurrentProjectMember() {
        Member member = Member.builder().id(2L).name("Employee").role(MemberRole.FUNCIONARIO).build();
        Project project = Project.builder().id(10L).status(ProjectStatus.PLANEJADO).build();
        project.getMembers().add(member);

        when(memberExternalClient.findByIds(Set.of(2L))).thenReturn(Map.of(
                2L, MemberResponse.builder().id(2L).name("Employee").role("funcionario").build()
        ));
        when(projectRepository.countActiveProjectsByMemberIds(eq(Set.of(2L)), any()))
                .thenReturn(Map.of(2L, 3L));
        when(projectRepository.findWithMembersById(10L)).thenReturn(java.util.Optional.of(project));
        when(memberRepository.findAllById(Set.of(2L))).thenReturn(List.of(member));

        memberAllocationService.resolveMembers(Set.of(2L), 10L);
    }
}
