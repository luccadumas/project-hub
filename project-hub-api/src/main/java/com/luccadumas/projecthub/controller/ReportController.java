package com.luccadumas.projecthub.controller;

import com.luccadumas.projecthub.dto.response.PortfolioReportResponse;
import com.luccadumas.projecthub.service.PortfolioReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Tag(name = "Reports", description = "Portfolio reports")
@SecurityRequirement(name = "bearerAuth")
public class ReportController {

    private final PortfolioReportService portfolioReportService;

    @GetMapping("/portfolio")
    @Operation(summary = "Generate portfolio summary report")
    public PortfolioReportResponse portfolioReport() {
        return portfolioReportService.generateReport();
    }
}
