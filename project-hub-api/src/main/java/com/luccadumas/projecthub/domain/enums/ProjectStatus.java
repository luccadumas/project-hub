package com.luccadumas.projecthub.domain.enums;

public enum ProjectStatus {
    UNDER_ANALYSIS,
    ANALYSIS_COMPLETED,
    ANALYSIS_APPROVED,
    STARTED,
    PLANNED,
    IN_PROGRESS,
    COMPLETED,
    CANCELED;

    public boolean isTerminal() {
        return this == COMPLETED || this == CANCELED;
    }

    public boolean blocksDeletion() {
        return this == STARTED || this == IN_PROGRESS || this == COMPLETED;
    }

    public boolean isActiveForMemberAllocation() {
        return this != COMPLETED && this != CANCELED;
    }
}
