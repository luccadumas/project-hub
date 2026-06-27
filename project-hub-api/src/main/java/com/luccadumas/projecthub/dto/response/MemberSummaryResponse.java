package com.luccadumas.projecthub.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MemberSummaryResponse {

    private Long id;
    private String name;
    private String role;
}
