package com.luccadumas.projecthub.service;

import com.luccadumas.projecthub.domain.enums.ProjectStatus;
import com.luccadumas.projecthub.dto.response.PortfolioReportResponse;
import com.luccadumas.projecthub.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PortfolioReportService {

    private final ProjectRepository projectRepository;

    public PortfolioReportResponse generateReport() {
        Map<ProjectStatus, Long> countByStatus = new EnumMap<>(ProjectStatus.class);
        Map<ProjectStatus, BigDecimal> budgetByStatus = new EnumMap<>(ProjectStatus.class);

        for (ProjectStatus status : ProjectStatus.values()) {
            countByStatus.put(status, 0L);
            budgetByStatus.put(status, BigDecimal.ZERO);
        }

        for (Object[] row : projectRepository.aggregateProjectsByStatus()) {
            ProjectStatus status = (ProjectStatus) row[0];
            countByStatus.put(status, (Long) row[1]);
            budgetByStatus.put(status, (BigDecimal) row[2]);
        }

        Double averageDuration = projectRepository.averageClosedProjectDurationDays();

        return PortfolioReportResponse.builder()
                .projectsCountByStatus(countByStatus)
                .totalBudgetByStatus(budgetByStatus)
                .averageClosedProjectDurationDays(averageDuration != null ? averageDuration : 0.0)
                .uniqueAllocatedMembers(projectRepository.countDistinctAllocatedMembers())
                .build();
    }
}
