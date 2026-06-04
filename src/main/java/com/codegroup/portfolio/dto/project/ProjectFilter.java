package com.codegroup.portfolio.dto.project;

import com.codegroup.portfolio.domain.enums.ProjectStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ProjectFilter(
        String name,
        ProjectStatus status,
        Long managerId,
        LocalDate startFrom,
        LocalDate startUntil,
        BigDecimal minBudget,
        BigDecimal maxBudget
) {
}
