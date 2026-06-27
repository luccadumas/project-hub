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
        assertThatCode(() -> workflow.validateTransition(ProjectStatus.EM_ANALISE, ProjectStatus.ANALISE_REALIZADA))
                .doesNotThrowAnyException();
        assertThatCode(() -> workflow.validateTransition(ProjectStatus.ANALISE_REALIZADA, ProjectStatus.ANALISE_APROVADA))
                .doesNotThrowAnyException();
        assertThatCode(() -> workflow.validateTransition(ProjectStatus.ANALISE_APROVADA, ProjectStatus.INICIADO))
                .doesNotThrowAnyException();
        assertThatCode(() -> workflow.validateTransition(ProjectStatus.INICIADO, ProjectStatus.PLANEJADO))
                .doesNotThrowAnyException();
        assertThatCode(() -> workflow.validateTransition(ProjectStatus.PLANEJADO, ProjectStatus.EM_ANDAMENTO))
                .doesNotThrowAnyException();
        assertThatCode(() -> workflow.validateTransition(ProjectStatus.EM_ANDAMENTO, ProjectStatus.ENCERRADO))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should block skipping workflow steps")
    void shouldBlockSkippingSteps() {
        assertThatThrownBy(() -> workflow.validateTransition(ProjectStatus.EM_ANALISE, ProjectStatus.INICIADO))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Invalid status transition");
    }

    @ParameterizedTest
    @EnumSource(value = ProjectStatus.class, names = {"EM_ANALISE", "ANALISE_REALIZADA", "ANALISE_APROVADA", "INICIADO", "PLANEJADO", "EM_ANDAMENTO"})
    @DisplayName("Should allow cancellation from active statuses")
    void shouldAllowCancellationFromActiveStatuses(ProjectStatus currentStatus) {
        assertThatCode(() -> workflow.validateTransition(currentStatus, ProjectStatus.CANCELADO))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should block cancellation from terminal statuses")
    void shouldBlockCancellationFromTerminalStatuses() {
        assertThatThrownBy(() -> workflow.validateTransition(ProjectStatus.ENCERRADO, ProjectStatus.CANCELADO))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Cannot cancel");
        assertThatThrownBy(() -> workflow.validateTransition(ProjectStatus.CANCELADO, ProjectStatus.CANCELADO))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("Should block transition from terminal statuses")
    void shouldBlockTransitionFromTerminalStatuses() {
        assertThatThrownBy(() -> workflow.validateTransition(ProjectStatus.ENCERRADO, ProjectStatus.EM_ANDAMENTO))
                .isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> workflow.validateTransition(ProjectStatus.CANCELADO, ProjectStatus.EM_ANALISE))
                .isInstanceOf(BusinessException.class);
    }
}
