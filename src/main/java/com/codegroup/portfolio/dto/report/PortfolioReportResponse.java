package com.codegroup.portfolio.dto.report;

import com.codegroup.portfolio.domain.enums.ProjectStatus;

import java.math.BigDecimal;
import java.util.Map;

public record PortfolioReportResponse(

        Map<ProjectStatus, Long> projectCountByStatus,

        Map<ProjectStatus, BigDecimal> totalBudgetByStatus,

        Double averageDurationDaysOfClosedProjects,

        long uniqueAllocatedMembers
) {
}
