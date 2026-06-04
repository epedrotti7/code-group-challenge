package com.codegroup.portfolio.domain.enums;

import java.util.List;
import java.util.Set;

public enum ProjectStatus {

    EM_ANALISE("em analise"),
    ANALISE_REALIZADA("analise realizada"),
    ANALISE_APROVADA("analise aprovada"),
    INICIADO("iniciado"),
    PLANEJADO("planejado"),
    EM_ANDAMENTO("em andamento"),
    ENCERRADO("encerrado"),
    CANCELADO("cancelado");

    private final String label;

    ProjectStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static final List<ProjectStatus> SEQUENCE = List.of(
            EM_ANALISE, ANALISE_REALIZADA, ANALISE_APROVADA,
            INICIADO, PLANEJADO, EM_ANDAMENTO, ENCERRADO);

    public static final Set<ProjectStatus> TERMINAL = Set.of(ENCERRADO, CANCELADO);

    public static final Set<ProjectStatus> NON_DELETABLE = Set.of(INICIADO, EM_ANDAMENTO, ENCERRADO);

    public boolean isTerminal() {
        return TERMINAL.contains(this);
    }
}
