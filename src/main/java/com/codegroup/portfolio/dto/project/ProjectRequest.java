package com.codegroup.portfolio.dto.project;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

public record ProjectRequest(

        @Schema(example = "Migracao de Datacenter")
        @NotBlank(message = "O nome e obrigatorio")
        String name,

        @Schema(example = "2026-01-10")
        @NotNull(message = "A data de inicio e obrigatoria")
        LocalDate startDate,

        @Schema(example = "2026-04-10")
        @NotNull(message = "A previsao de termino e obrigatoria")
        LocalDate expectedEndDate,

        @Schema(example = "150000.00")
        @NotNull(message = "O orcamento total e obrigatorio")
        @Positive(message = "O orcamento total deve ser positivo")
        BigDecimal totalBudget,

        @Schema(example = "Migracao completa do datacenter on-premise para a nuvem")
        String description,

        @Schema(example = "1", description = "Id do membro gerente responsavel")
        @NotNull(message = "O gerente responsavel e obrigatorio")
        Long managerId,

        @Schema(example = "[2, 3]", description = "Ids dos membros (funcionarios) a alocar")
        Set<Long> memberIds
) {
}
