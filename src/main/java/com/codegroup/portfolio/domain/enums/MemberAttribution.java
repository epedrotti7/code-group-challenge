package com.codegroup.portfolio.domain.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum MemberAttribution {
    FUNCIONARIO("funcionario"),
    GERENTE("gerente"),
    TERCEIRO("terceiro"),
    ESTAGIARIO("estagiario");

    private final String label;

    MemberAttribution(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    @JsonCreator
    public static MemberAttribution from(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim().toLowerCase();
        for (MemberAttribution attribution : values()) {
            if (attribution.name().equalsIgnoreCase(normalized)
                    || attribution.label.equalsIgnoreCase(normalized)) {
                return attribution;
            }
        }
        throw new IllegalArgumentException("Atribuicao invalida: " + value);
    }
}
