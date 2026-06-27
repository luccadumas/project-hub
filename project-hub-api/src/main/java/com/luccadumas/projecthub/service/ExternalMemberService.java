package com.luccadumas.projecthub.service;

import com.luccadumas.projecthub.domain.enums.MemberRole;
import com.luccadumas.projecthub.domain.enums.ProjectStatus;
import com.luccadumas.projecthub.dto.request.MemberCreateRequest;
import com.luccadumas.projecthub.dto.response.MemberResponse;
import com.luccadumas.projecthub.entity.Member;
import com.luccadumas.projecthub.exception.BusinessException;
import com.luccadumas.projecthub.exception.ResourceNotFoundException;
import com.luccadumas.projecthub.mapper.MemberMapper;
import com.luccadumas.projecthub.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExternalMemberService {

    private final MemberRepository memberRepository;
    private final MemberMapper memberMapper;

    public List<MemberResponse> findAll() {
        return memberRepository.findAll().stream()
                .map(memberMapper::toResponse)
                .toList();
    }

    public MemberResponse findById(Long id) {
        return memberRepository.findById(id)
                .map(memberMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with id: " + id));
    }

    @Transactional
    public MemberResponse create(MemberCreateRequest request) {
        MemberRole role;
        try {
            role = MemberRole.fromValue(request.getRole());
        } catch (IllegalArgumentException ex) {
            throw new BusinessException("Invalid member role: " + request.getRole());
        }

        Member member = Member.builder()
                .name(request.getName())
                .role(role)
                .build();

        return memberMapper.toResponse(memberRepository.save(member));
    }
}
