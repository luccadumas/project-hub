package com.luccadumas.projecthub.dto.response;

import com.luccadumas.projecthub.domain.enums.ProjectStatus;
import com.luccadumas.projecthub.domain.enums.RiskLevel;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

@Getter
@Builder
public class ProjectResponse {

    private Long id;
    private String name;
    private LocalDate startDate;
    private LocalDate expectedEndDate;
    private LocalDate actualEndDate;
    private BigDecimal totalBudget;
    private String description;
    private Long managerId;
    private String managerName;
    private ProjectStatus status;
    private RiskLevel riskLevel;
    private Set<MemberSummaryResponse> members;
}
