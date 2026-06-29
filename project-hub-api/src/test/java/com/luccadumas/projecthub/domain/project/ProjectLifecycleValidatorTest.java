package com.luccadumas.projecthub.domain.project;

import com.luccadumas.projecthub.domain.enums.MemberRole;
import com.luccadumas.projecthub.domain.enums.ProjectStatus;
import com.luccadumas.projecthub.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProjectLifecycleValidatorTest {

    private ProjectLifecycleValidator validator;

    @BeforeEach
    void setUp() {
        validator = new ProjectLifecycleValidator();
    }

    @Test
    @DisplayName("Should allow modification for active statuses")
    void shouldAllowModificationForActiveStatuses() {
        assertThatCode(() -> validator.ensureModifiable(ProjectStatus.IN_PROGRESS))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should block modification for terminal statuses")
    void shouldBlockModificationForTerminalStatuses() {
        assertThatThrownBy(() -> validator.ensureModifiable(ProjectStatus.COMPLETED))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("cannot be modified");
    }

    @Test
    @DisplayName("Should accept manager as project manager")
    void shouldAcceptGerenteAsManager() {
        assertThatCode(() -> validator.validateManager(MemberRole.MANAGER, 1L))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should reject non-manager as project manager")
    void shouldRejectNonGerenteAsManager() {
        assertThatThrownBy(() -> validator.validateManager(MemberRole.EMPLOYEE, 2L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("manager");
    }
}
