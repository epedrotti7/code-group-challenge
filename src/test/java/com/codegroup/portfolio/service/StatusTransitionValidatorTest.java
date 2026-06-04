package com.codegroup.portfolio.service;

import com.codegroup.portfolio.domain.enums.ProjectStatus;
import com.codegroup.portfolio.exception.BusinessException;
import com.codegroup.portfolio.exception.InvalidStatusTransitionException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StatusTransitionValidatorTest {

    private final StatusTransitionValidator validator = new StatusTransitionValidator();

    @Test
    void shouldAllowAdvancingOneStep() {
        assertThatCode(() -> validator.validate(ProjectStatus.EM_ANALISE, ProjectStatus.ANALISE_REALIZADA))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldRejectSkippingSteps() {
        assertThatThrownBy(() -> validator.validate(ProjectStatus.EM_ANALISE, ProjectStatus.INICIADO))
                .isInstanceOf(InvalidStatusTransitionException.class);
    }

    @Test
    void shouldRejectGoingBackwards() {
        assertThatThrownBy(() -> validator.validate(ProjectStatus.INICIADO, ProjectStatus.EM_ANALISE))
                .isInstanceOf(InvalidStatusTransitionException.class);
    }

    @Test
    void shouldAllowCancelFromAnyNonTerminalState() {
        assertThatCode(() -> validator.validate(ProjectStatus.PLANEJADO, ProjectStatus.CANCELADO))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldRejectTransitionFromTerminalState() {
        assertThatThrownBy(() -> validator.validate(ProjectStatus.ENCERRADO, ProjectStatus.CANCELADO))
                .isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> validator.validate(ProjectStatus.CANCELADO, ProjectStatus.EM_ANDAMENTO))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void shouldRejectSameStatus() {
        assertThatThrownBy(() -> validator.validate(ProjectStatus.INICIADO, ProjectStatus.INICIADO))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void shouldRejectNullTarget() {
        assertThatThrownBy(() -> validator.validate(ProjectStatus.INICIADO, null))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void shouldWalkFullHappyPath() {
        assertThatCode(() -> {
            validator.validate(ProjectStatus.EM_ANALISE, ProjectStatus.ANALISE_REALIZADA);
            validator.validate(ProjectStatus.ANALISE_REALIZADA, ProjectStatus.ANALISE_APROVADA);
            validator.validate(ProjectStatus.ANALISE_APROVADA, ProjectStatus.INICIADO);
            validator.validate(ProjectStatus.INICIADO, ProjectStatus.PLANEJADO);
            validator.validate(ProjectStatus.PLANEJADO, ProjectStatus.EM_ANDAMENTO);
            validator.validate(ProjectStatus.EM_ANDAMENTO, ProjectStatus.ENCERRADO);
        }).doesNotThrowAnyException();
    }
}
