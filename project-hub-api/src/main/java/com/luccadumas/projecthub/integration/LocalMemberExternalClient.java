package com.luccadumas.projecthub.integration;

import com.luccadumas.projecthub.dto.response.MemberResponse;
import com.luccadumas.projecthub.service.ExternalMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Primary
@RequiredArgsConstructor
public class LocalMemberExternalClient implements MemberExternalClient {

    private final ExternalMemberService externalMemberService;

    @Override
    public MemberResponse findById(Long id) {
        return externalMemberService.findById(id);
    }

    @Override
    public List<MemberResponse> findAll() {
        return externalMemberService.findAll();
    }
}
