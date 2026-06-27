package com.luccadumas.projecthub.dto.response;

import com.luccadumas.projecthub.domain.enums.ProjectStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.Map;

@Getter
@Builder
public class PortfolioReportResponse {

    private Map<ProjectStatus, Long> projectsCountByStatus;
    private Map<ProjectStatus, BigDecimal> totalBudgetByStatus;
    private Double averageClosedProjectDurationDays;
    private Long uniqueAllocatedMembers;
}
