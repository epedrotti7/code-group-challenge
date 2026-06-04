package com.codegroup.portfolio.service;

import com.codegroup.portfolio.domain.entity.Project;
import com.codegroup.portfolio.domain.enums.RiskLevel;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Component
public class RiskCalculator {

    private static final BigDecimal LOW_BUDGET_LIMIT = new BigDecimal("100000");
    private static final BigDecimal HIGH_BUDGET_LIMIT = new BigDecimal("500000");
    private static final long LOW_MONTHS_LIMIT = 3;
    private static final long HIGH_MONTHS_LIMIT = 6;

    public RiskLevel calculate(Project project) {
        return calculate(project.getTotalBudget(), project.getStartDate(), project.getExpectedEndDate());
    }

    public RiskLevel calculate(BigDecimal budget, LocalDate startDate, LocalDate expectedEndDate) {
        if (budget == null || startDate == null || expectedEndDate == null) {
            throw new IllegalArgumentException("Orcamento, data de inicio e previsao de termino sao obrigatorios para o calculo de risco");
        }
        long months = monthsBetween(startDate, expectedEndDate);

        if (budget.compareTo(HIGH_BUDGET_LIMIT) > 0 || months > HIGH_MONTHS_LIMIT) {
            return RiskLevel.ALTO;
        }
        if (budget.compareTo(LOW_BUDGET_LIMIT) > 0 || months > LOW_MONTHS_LIMIT) {
            return RiskLevel.MEDIO;
        }
        return RiskLevel.BAIXO;
    }

    private long monthsBetween(LocalDate start, LocalDate end) {
        long fullMonths = ChronoUnit.MONTHS.between(start, end);
        LocalDate afterFullMonths = start.plusMonths(fullMonths);
        if (afterFullMonths.isBefore(end)) {
            fullMonths += 1;
        }
        return fullMonths;
    }
}
