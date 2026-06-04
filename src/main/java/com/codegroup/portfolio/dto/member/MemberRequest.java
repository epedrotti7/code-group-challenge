package com.codegroup.portfolio.dto.member;

import com.codegroup.portfolio.domain.enums.MemberAttribution;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import io.swagger.v3.oas.annotations.media.Schema;

public record MemberRequest(

        @Schema(example = "Maria Silva")
        @NotBlank(message = "O nome e obrigatorio")
        String name,

        @Schema(example = "funcionario", description = "Atribuicao/cargo do membro")
        @NotNull(message = "A atribuicao e obrigatoria")
        MemberAttribution attribution
) {
}
