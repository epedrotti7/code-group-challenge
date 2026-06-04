package com.codegroup.portfolio.service;

import com.codegroup.portfolio.domain.enums.RiskLevel;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RiskCalculatorTest {

    private final RiskCalculator calculator = new RiskCalculator();

    private static final LocalDate START = LocalDate.of(2026, 1, 1);

    @Test
    void shouldClassifyAsLowRisk() {
        RiskLevel risk = calculator.calculate(new BigDecimal("100000"), START, START.plusMonths(3));
        assertThat(risk).isEqualTo(RiskLevel.BAIXO);
    }

    @Test
    void shouldClassifyAsMediumRiskByBudget() {
        RiskLevel risk = calculator.calculate(new BigDecimal("100001"), START, START.plusMonths(2));
        assertThat(risk).isEqualTo(RiskLevel.MEDIO);
    }

    @Test
    void shouldClassifyAsMediumRiskByDuration() {
        RiskLevel risk = calculator.calculate(new BigDecimal("50000"), START, START.plusMonths(5));
        assertThat(risk).isEqualTo(RiskLevel.MEDIO);
    }

    @Test
    void shouldClassifyAsMediumRiskOnUpperBudgetBoundary() {
        RiskLevel risk = calculator.calculate(new BigDecimal("500000"), START, START.plusMonths(1));
        assertThat(risk).isEqualTo(RiskLevel.MEDIO);
    }

    @Test
    void shouldClassifyAsHighRiskByBudget() {
        RiskLevel risk = calculator.calculate(new BigDecimal("500001"), START, START.plusMonths(1));
        assertThat(risk).isEqualTo(RiskLevel.ALTO);
    }

    @Test
    void shouldClassifyAsHighRiskByDuration() {
        RiskLevel risk = calculator.calculate(new BigDecimal("10000"), START, START.plusMonths(7));
        assertThat(risk).isEqualTo(RiskLevel.ALTO);
    }

    @Test
    void shouldClassifyExactlySixMonthsAsMediumNotHigh() {
        RiskLevel risk = calculator.calculate(new BigDecimal("10000"), START, START.plusMonths(6));
        assertThat(risk).isEqualTo(RiskLevel.MEDIO);
    }

    @Test
    void shouldTreatPartialMonthBeyondThreeAsMedium() {
        RiskLevel risk = calculator.calculate(new BigDecimal("10000"), START, START.plusMonths(3).plusDays(1));
        assertThat(risk).isEqualTo(RiskLevel.MEDIO);
    }

    @Test
    void shouldFailWhenDataIsMissing() {
        assertThatThrownBy(() -> calculator.calculate(null, START, START.plusMonths(1)))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
