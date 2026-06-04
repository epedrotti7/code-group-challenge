package com.codegroup.portfolio.controller;

import com.codegroup.portfolio.dto.report.PortfolioReportResponse;
import com.codegroup.portfolio.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Relatorios", description = "Relatorio resumido do portfolio")
@RestController
@RequestMapping("/api/v1/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @Operation(summary = "Gera o relatorio resumido do portfolio")
    @GetMapping("/portfolio")
    public PortfolioReportResponse portfolio() {
        return reportService.generate();
    }
}
