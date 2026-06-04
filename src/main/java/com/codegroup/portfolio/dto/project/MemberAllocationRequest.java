package com.codegroup.portfolio.dto.project;

import jakarta.validation.constraints.NotNull;

public record MemberAllocationRequest(

        @NotNull(message = "O id do membro e obrigatorio")
        Long memberId
) {
}
