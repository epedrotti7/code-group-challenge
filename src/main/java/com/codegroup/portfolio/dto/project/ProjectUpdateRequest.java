package com.codegroup.portfolio.dto.project;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ProjectUpdateRequest(

        @NotBlank(message = "O nome e obrigatorio")
        String name,

        @NotNull(message = "A data de inicio e obrigatoria")
        LocalDate startDate,

        @NotNull(message = "A previsao de termino e obrigatoria")
        LocalDate expectedEndDate,

        @Schema(description = "Data real de termino (opcional)")
        LocalDate actualEndDate,

        @NotNull(message = "O orcamento total e obrigatorio")
        @Positive(message = "O orcamento total deve ser positivo")
        BigDecimal totalBudget,

        String description,

        @NotNull(message = "O gerente responsavel e obrigatorio")
        Long managerId
) {
}
