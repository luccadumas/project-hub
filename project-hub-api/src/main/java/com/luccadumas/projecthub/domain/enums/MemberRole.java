package com.luccadumas.projecthub.domain.enums;

import com.luccadumas.projecthub.exception.BusinessException;

public enum MemberRole {
    FUNCIONARIO,
    GERENTE,
    ESTAGIARIO,
    CONSULTOR;

    public static MemberRole fromValue(String value) {
        return MemberRole.valueOf(value.trim().toUpperCase());
    }

    public boolean canBeAllocatedToProject() {
        return this == FUNCIONARIO;
    }

    public boolean canBeManager() {
        return this == GERENTE;
    }

    public static MemberRole parseRole(String value) {
        try {
            return fromValue(value);
        } catch (IllegalArgumentException ex) {
            throw new BusinessException("Invalid member role: " + value);
        }
    }
}
