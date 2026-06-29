package com.luccadumas.projecthub.domain.status;

import com.luccadumas.projecthub.domain.enums.ProjectStatus;
import com.luccadumas.projecthub.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProjectStatusWorkflowTest {

    private ProjectStatusWorkflow workflow;

    @BeforeEach
    void setUp() {
        workflow = new ProjectStatusWorkflow();
    }

    @Test
    @DisplayName("Should allow sequential status transitions")
    void shouldAllowSequentialTransitions() {
        assertThatCode(() -> workflow.validateTransition(ProjectStatus.UNDER_ANALYSIS, ProjectStatus.ANALYSIS_COMPLETED))
                .doesNotThrowAnyException();
        assertThatCode(() -> workflow.validateTransition(ProjectStatus.ANALYSIS_COMPLETED, ProjectStatus.ANALYSIS_APPROVED))
                .doesNotThrowAnyException();
        assertThatCode(() -> workflow.validateTransition(ProjectStatus.ANALYSIS_APPROVED, ProjectStatus.STARTED))
                .doesNotThrowAnyException();
        assertThatCode(() -> workflow.validateTransition(ProjectStatus.STARTED, ProjectStatus.PLANNED))
                .doesNotThrowAnyException();
        assertThatCode(() -> workflow.validateTransition(ProjectStatus.PLANNED, ProjectStatus.IN_PROGRESS))
                .doesNotThrowAnyException();
        assertThatCode(() -> workflow.validateTransition(ProjectStatus.IN_PROGRESS, ProjectStatus.COMPLETED))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should block skipping workflow steps")
    void shouldBlockSkippingSteps() {
        assertThatThrownBy(() -> workflow.validateTransition(ProjectStatus.UNDER_ANALYSIS, ProjectStatus.STARTED))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Invalid status transition");
    }

    @ParameterizedTest
    @EnumSource(value = ProjectStatus.class, names = {"UNDER_ANALYSIS", "ANALYSIS_COMPLETED", "ANALYSIS_APPROVED", "STARTED", "PLANNED", "IN_PROGRESS"})
    @DisplayName("Should allow cancellation from active statuses")
    void shouldAllowCancellationFromActiveStatuses(ProjectStatus currentStatus) {
        assertThatCode(() -> workflow.validateTransition(currentStatus, ProjectStatus.CANCELED))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should block cancellation from terminal statuses")
    void shouldBlockCancellationFromTerminalStatuses() {
        assertThatThrownBy(() -> workflow.validateTransition(ProjectStatus.COMPLETED, ProjectStatus.CANCELED))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Cannot cancel");
        assertThatThrownBy(() -> workflow.validateTransition(ProjectStatus.CANCELED, ProjectStatus.CANCELED))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("Should block transition from terminal statuses")
    void shouldBlockTransitionFromTerminalStatuses() {
        assertThatThrownBy(() -> workflow.validateTransition(ProjectStatus.COMPLETED, ProjectStatus.IN_PROGRESS))
                .isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> workflow.validateTransition(ProjectStatus.CANCELED, ProjectStatus.UNDER_ANALYSIS))
                .isInstanceOf(BusinessException.class);
    }
}
