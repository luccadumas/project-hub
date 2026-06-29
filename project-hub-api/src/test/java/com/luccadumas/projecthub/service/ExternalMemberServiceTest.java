package com.luccadumas.projecthub.service;

import com.luccadumas.projecthub.domain.enums.MemberRole;
import com.luccadumas.projecthub.dto.request.MemberCreateRequest;
import com.luccadumas.projecthub.dto.response.MemberResponse;
import com.luccadumas.projecthub.entity.Member;
import com.luccadumas.projecthub.exception.BusinessException;
import com.luccadumas.projecthub.exception.ResourceNotFoundException;
import com.luccadumas.projecthub.mapper.MemberMapper;
import com.luccadumas.projecthub.mapper.MemberMapperImpl;
import com.luccadumas.projecthub.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExternalMemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Spy
    private MemberMapper memberMapper = new MemberMapperImpl();

    @InjectMocks
    private ExternalMemberService externalMemberService;

    @Test
    @DisplayName("Should list all members")
    void shouldListAllMembers() {
        Member member = Member.builder().id(1L).name("Ana Silva").role(MemberRole.MANAGER).build();
        when(memberRepository.findAll()).thenReturn(List.of(member));

        List<MemberResponse> members = externalMemberService.findAll();

        assertThat(members).hasSize(1);
        assertThat(members.getFirst().getRole()).isEqualTo("manager");
    }

    @Test
    @DisplayName("Should find member by id")
    void shouldFindMemberById() {
        Member member = Member.builder().id(2L).name("Bruno Costa").role(MemberRole.EMPLOYEE).build();
        when(memberRepository.findById(2L)).thenReturn(Optional.of(member));

        MemberResponse response = externalMemberService.findById(2L);

        assertThat(response.getName()).isEqualTo("Bruno Costa");
    }

    @Test
    @DisplayName("Should throw when member is not found")
    void shouldThrowWhenMemberNotFound() {
        when(memberRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> externalMemberService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("Should create member with valid role")
    void shouldCreateMemberWithValidRole() {
        MemberCreateRequest request = new MemberCreateRequest();
        request.setName("Fernando Lima");
        request.setRole("MANAGER");

        when(memberRepository.save(any(Member.class))).thenAnswer(invocation -> {
            Member saved = invocation.getArgument(0);
            saved.setId(6L);
            return saved;
        });

        MemberResponse response = externalMemberService.create(request);

        ArgumentCaptor<Member> captor = ArgumentCaptor.forClass(Member.class);
        verify(memberRepository).save(captor.capture());
        assertThat(captor.getValue().getRole()).isEqualTo(MemberRole.MANAGER);
        assertThat(response.getId()).isEqualTo(6L);
        assertThat(response.getRole()).isEqualTo("manager");
    }

    @Test
    @DisplayName("Should reject member creation with invalid role")
    void shouldRejectInvalidRole() {
        MemberCreateRequest request = new MemberCreateRequest();
        request.setName("Invalid");
        request.setRole("INVALID");

        assertThatThrownBy(() -> externalMemberService.create(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Invalid member role");
    }
}
