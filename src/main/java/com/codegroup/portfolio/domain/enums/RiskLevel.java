package com.codegroup.portfolio.domain.enums;

public enum RiskLevel {
    BAIXO("Baixo risco"),
    MEDIO("Medio risco"),
    ALTO("Alto risco");

    private final String label;

    RiskLevel(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
