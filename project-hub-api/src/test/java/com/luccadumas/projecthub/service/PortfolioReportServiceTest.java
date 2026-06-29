package com.luccadumas.projecthub.service;

import com.luccadumas.projecthub.domain.enums.ProjectStatus;
import com.luccadumas.projecthub.dto.response.PortfolioReportResponse;
import com.luccadumas.projecthub.repository.ProjectRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PortfolioReportServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @InjectMocks
    private PortfolioReportService portfolioReportService;

    @Test
    @DisplayName("Should generate portfolio report with aggregated metrics")
    void shouldGeneratePortfolioReport() {
        when(projectRepository.aggregateProjectsByStatus()).thenReturn(List.of(
                new Object[]{ProjectStatus.COMPLETED, 1L, new BigDecimal("100000")},
                new Object[]{ProjectStatus.IN_PROGRESS, 1L, new BigDecimal("50000")}
        ));
        when(projectRepository.averageClosedProjectDurationDays()).thenReturn(60.0);
        when(projectRepository.countDistinctAllocatedMembers()).thenReturn(4L);

        PortfolioReportResponse report = portfolioReportService.generateReport();

        assertThat(report.getProjectsCountByStatus().get(ProjectStatus.COMPLETED)).isEqualTo(1L);
        assertThat(report.getProjectsCountByStatus().get(ProjectStatus.IN_PROGRESS)).isEqualTo(1L);
        assertThat(report.getTotalBudgetByStatus().get(ProjectStatus.COMPLETED))
                .isEqualByComparingTo(new BigDecimal("100000"));
        assertThat(report.getAverageClosedProjectDurationDays()).isEqualTo(60.0);
        assertThat(report.getUniqueAllocatedMembers()).isEqualTo(4L);
    }

    @Test
    @DisplayName("Should default average duration to zero when repository returns null")
    void shouldDefaultAverageDurationWhenNull() {
        when(projectRepository.aggregateProjectsByStatus()).thenReturn(List.of());
        when(projectRepository.averageClosedProjectDurationDays()).thenReturn(null);
        when(projectRepository.countDistinctAllocatedMembers()).thenReturn(0L);

        PortfolioReportResponse report = portfolioReportService.generateReport();

        assertThat(report.getAverageClosedProjectDurationDays()).isZero();
        assertThat(report.getUniqueAllocatedMembers()).isZero();
    }
}
