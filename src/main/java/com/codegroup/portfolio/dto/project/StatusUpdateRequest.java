package com.codegroup.portfolio.dto.project;

import com.codegroup.portfolio.domain.enums.ProjectStatus;
import jakarta.validation.constraints.NotNull;

public record StatusUpdateRequest(

        @NotNull(message = "O novo status e obrigatorio")
        ProjectStatus status
) {
}
