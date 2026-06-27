package com.luccadumas.projecthub.integration;

import com.luccadumas.projecthub.dto.response.MemberResponse;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public interface MemberExternalClient {

    MemberResponse findById(Long id);

    List<MemberResponse> findAll();

    default Map<Long, MemberResponse> findByIds(Set<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Map.of();
        }
        return findAll().stream()
                .filter(member -> ids.contains(member.getId()))
                .collect(Collectors.toMap(MemberResponse::getId, Function.identity(), (first, second) -> first));
    }
}
