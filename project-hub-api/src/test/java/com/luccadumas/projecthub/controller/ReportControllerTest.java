package com.luccadumas.projecthub.controller;

import com.luccadumas.projecthub.config.JwtProperties;
import com.luccadumas.projecthub.config.SecurityConfig;
import com.luccadumas.projecthub.config.SecurityProperties;
import com.luccadumas.projecthub.domain.enums.ProjectStatus;
import com.luccadumas.projecthub.dto.response.PortfolioReportResponse;
import com.luccadumas.projecthub.exception.GlobalExceptionHandler;
import com.luccadumas.projecthub.security.DatabaseUserDetailsService;
import com.luccadumas.projecthub.security.JwtAuthenticationFilter;
import com.luccadumas.projecthub.security.JwtService;
import com.luccadumas.projecthub.service.PortfolioReportService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.EnumMap;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ReportController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class, JwtAuthenticationFilter.class, JwtService.class, DatabaseUserDetailsService.class})
@EnableConfigurationProperties({SecurityProperties.class, JwtProperties.class})
@TestPropertySource(properties = {
        "project-hub.jwt.secret=test-secret-key-at-least-32-characters-long",
        "project-hub.jwt.access-token-expiration-minutes=15",
        "project-hub.jwt.refresh-token-expiration-days=7"
})
class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PortfolioReportService portfolioReportService;

    @MockBean
    private com.luccadumas.projecthub.repository.AppUserRepository appUserRepository;

    @Test
    @DisplayName("Should require authentication for portfolio report")
    void shouldRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/reports/portfolio"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Should return portfolio report for authenticated user")
    void shouldReturnPortfolioReport() throws Exception {
        var counts = new EnumMap<ProjectStatus, Long>(ProjectStatus.class);
        counts.put(ProjectStatus.IN_PROGRESS, 2L);
        var budgets = new EnumMap<ProjectStatus, BigDecimal>(ProjectStatus.class);
        budgets.put(ProjectStatus.IN_PROGRESS, new BigDecimal("150000"));

        when(portfolioReportService.generateReport()).thenReturn(
                PortfolioReportResponse.builder()
                        .projectsCountByStatus(counts)
                        .totalBudgetByStatus(budgets)
                        .averageClosedProjectDurationDays(45.0)
                        .uniqueAllocatedMembers(5L)
                        .build()
        );

        mockMvc.perform(get("/api/reports/portfolio"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uniqueAllocatedMembers").value(5))
                .andExpect(jsonPath("$.averageClosedProjectDurationDays").value(45.0))
                .andExpect(jsonPath("$.projectsCountByStatus.IN_PROGRESS").value(2));
    }
}
