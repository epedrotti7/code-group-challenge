package com.codegroup.portfolio.service;

import com.codegroup.portfolio.domain.enums.ProjectStatus;
import com.codegroup.portfolio.exception.BusinessException;
import com.codegroup.portfolio.exception.InvalidStatusTransitionException;
import org.springframework.stereotype.Component;

@Component
public class StatusTransitionValidator {

    public void validate(ProjectStatus current, ProjectStatus target) {
        if (target == null) {
            throw new BusinessException("O status de destino e obrigatorio");
        }
        if (current == target) {
            throw new BusinessException("O projeto ja esta no status '" + target.getLabel() + "'");
        }
        if (current.isTerminal()) {
            throw new BusinessException("O projeto esta em estado terminal ('"
                    + current.getLabel() + "') e nao admite novas transicoes");
        }
        if (target == ProjectStatus.CANCELADO) {
            return;
        }
        if (current == ProjectStatus.CANCELADO) {
            throw new InvalidStatusTransitionException(current, target);
        }
        int currentIndex = ProjectStatus.SEQUENCE.indexOf(current);
        int targetIndex = ProjectStatus.SEQUENCE.indexOf(target);
        if (targetIndex != currentIndex + 1) {
            throw new InvalidStatusTransitionException(current, target);
        }
    }
}
