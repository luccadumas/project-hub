package com.luccadumas.projecthub.domain.enums;

public enum ProjectStatus {
    EM_ANALISE,
    ANALISE_REALIZADA,
    ANALISE_APROVADA,
    INICIADO,
    PLANEJADO,
    EM_ANDAMENTO,
    ENCERRADO,
    CANCELADO;

    public boolean isTerminal() {
        return this == ENCERRADO || this == CANCELADO;
    }

    public boolean blocksDeletion() {
        return this == INICIADO || this == EM_ANDAMENTO || this == ENCERRADO;
    }

    public boolean isActiveForMemberAllocation() {
        return this != ENCERRADO && this != CANCELADO;
    }
}
