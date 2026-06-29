package com.luccadumas.projecthub.domain.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

class ProjectStatusTest {

    @ParameterizedTest
    @EnumSource(value = ProjectStatus.class, names = {"COMPLETED", "CANCELED"})
    @DisplayName("Should identify terminal statuses")
    void shouldIdentifyTerminalStatuses(ProjectStatus status) {
        assertThat(status.isTerminal()).isTrue();
        assertThat(status.isActiveForMemberAllocation()).isFalse();
    }

    @Test
    @DisplayName("Should block deletion for active and completed projects")
    void shouldBlockDeletionForProtectedStatuses() {
        assertThat(ProjectStatus.STARTED.blocksDeletion()).isTrue();
        assertThat(ProjectStatus.IN_PROGRESS.blocksDeletion()).isTrue();
        assertThat(ProjectStatus.COMPLETED.blocksDeletion()).isTrue();
        assertThat(ProjectStatus.PLANNED.blocksDeletion()).isFalse();
    }

    @Test
    @DisplayName("Should allow member allocation for non-terminal statuses")
    void shouldAllowMemberAllocationForActiveStatuses() {
        assertThat(ProjectStatus.IN_PROGRESS.isActiveForMemberAllocation()).isTrue();
        assertThat(ProjectStatus.UNDER_ANALYSIS.isActiveForMemberAllocation()).isTrue();
    }
}
