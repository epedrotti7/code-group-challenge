package com.codegroup.portfolio.dto.project;

import com.codegroup.portfolio.domain.enums.ProjectStatus;
import com.codegroup.portfolio.domain.enums.RiskLevel;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

public record ProjectResponse(
        Long id,
        String name,
        LocalDate startDate,
        LocalDate expectedEndDate,
        LocalDate actualEndDate,
        BigDecimal totalBudget,
        String description,
        Long managerId,
        ProjectStatus status,
        RiskLevel riskLevel,
        Set<Long> memberIds
) {
}
