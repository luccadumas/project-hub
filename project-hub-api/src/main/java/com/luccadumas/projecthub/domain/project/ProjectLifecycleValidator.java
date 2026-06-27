package com.luccadumas.projecthub.domain.project;

import com.luccadumas.projecthub.domain.enums.MemberRole;
import com.luccadumas.projecthub.domain.enums.ProjectStatus;
import com.luccadumas.projecthub.exception.BusinessException;
import org.springframework.stereotype.Component;

@Component
public class ProjectLifecycleValidator {

    public void ensureModifiable(ProjectStatus status) {
        if (status.isTerminal()) {
            throw new BusinessException("Project cannot be modified while status is " + status);
        }
    }

    public void validateManager(MemberRole role, Long memberId) {
        if (!role.canBeManager()) {
            throw new BusinessException(
                    "Only members with role 'gerente' can be assigned as project manager. Member id: " + memberId);
        }
    }
}
