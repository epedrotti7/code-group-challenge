package com.codegroup.portfolio.exception;

import com.codegroup.portfolio.domain.enums.ProjectStatus;

public class InvalidStatusTransitionException extends BusinessException {

    public InvalidStatusTransitionException(ProjectStatus from, ProjectStatus to) {
        super("Transicao de status invalida: de '" + from.getLabel()
                + "' para '" + to.getLabel() + "'. Nao e permitido pular etapas.");
    }
}
