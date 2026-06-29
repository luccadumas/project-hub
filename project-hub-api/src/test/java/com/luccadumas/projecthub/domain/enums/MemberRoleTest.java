package com.luccadumas.projecthub.domain.enums;

import com.luccadumas.projecthub.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MemberRoleTest {

    @ParameterizedTest
    @EnumSource(MemberRole.class)
    @DisplayName("Should parse role from uppercase and lowercase values")
    void shouldParseRoleFromValue(MemberRole role) {
        assertThat(MemberRole.fromValue(role.name())).isEqualTo(role);
        assertThat(MemberRole.fromValue(role.name().toLowerCase())).isEqualTo(role);
        assertThat(MemberRole.parseRole(role.name().toLowerCase())).isEqualTo(role);
    }

    @Test
    @DisplayName("Should identify manager and employee capabilities")
    void shouldIdentifyRoleCapabilities() {
        assertThat(MemberRole.MANAGER.canBeManager()).isTrue();
        assertThat(MemberRole.MANAGER.canBeAllocatedToProject()).isFalse();
        assertThat(MemberRole.EMPLOYEE.canBeAllocatedToProject()).isTrue();
        assertThat(MemberRole.INTERN.canBeManager()).isFalse();
    }

    @Test
    @DisplayName("Should reject invalid role values")
    void shouldRejectInvalidRole() {
        assertThatThrownBy(() -> MemberRole.parseRole("invalid"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Invalid member role");
    }
}
