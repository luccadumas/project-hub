package com.luccadumas.projecthub.service;

import com.luccadumas.projecthub.dto.response.MemberResponse;
import com.luccadumas.projecthub.integration.MemberExternalClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MemberDirectory {

    private final MemberExternalClient memberExternalClient;

    public MemberResponse getRequired(Long id) {
        return memberExternalClient.findById(id);
    }

    public Map<Long, String> resolveManagerNames(Set<Long> managerIds) {
        if (managerIds.isEmpty()) {
            return Map.of();
        }

        return memberExternalClient.findAll().stream()
                .filter(member -> managerIds.contains(member.getId()))
                .collect(Collectors.toMap(
                        MemberResponse::getId,
                        MemberResponse::getName,
                        (first, second) -> first
                ));
    }
}
