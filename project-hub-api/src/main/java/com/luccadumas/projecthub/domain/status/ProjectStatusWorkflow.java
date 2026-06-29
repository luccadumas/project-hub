package com.luccadumas.projecthub.domain.status;

import com.luccadumas.projecthub.domain.enums.ProjectStatus;
import com.luccadumas.projecthub.exception.BusinessException;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

@Component
public class ProjectStatusWorkflow {

    private static final Map<ProjectStatus, Set<ProjectStatus>> ALLOWED_TRANSITIONS = new EnumMap<>(ProjectStatus.class);

    static {
        ALLOWED_TRANSITIONS.put(ProjectStatus.UNDER_ANALYSIS, Set.of(ProjectStatus.ANALYSIS_COMPLETED, ProjectStatus.CANCELED));
        ALLOWED_TRANSITIONS.put(ProjectStatus.ANALYSIS_COMPLETED, Set.of(ProjectStatus.ANALYSIS_APPROVED, ProjectStatus.CANCELED));
        ALLOWED_TRANSITIONS.put(ProjectStatus.ANALYSIS_APPROVED, Set.of(ProjectStatus.STARTED, ProjectStatus.CANCELED));
        ALLOWED_TRANSITIONS.put(ProjectStatus.STARTED, Set.of(ProjectStatus.PLANNED, ProjectStatus.CANCELED));
        ALLOWED_TRANSITIONS.put(ProjectStatus.PLANNED, Set.of(ProjectStatus.IN_PROGRESS, ProjectStatus.CANCELED));
        ALLOWED_TRANSITIONS.put(ProjectStatus.IN_PROGRESS, Set.of(ProjectStatus.COMPLETED, ProjectStatus.CANCELED));
        ALLOWED_TRANSITIONS.put(ProjectStatus.COMPLETED, Set.of());
        ALLOWED_TRANSITIONS.put(ProjectStatus.CANCELED, Set.of());
    }

    public void validateTransition(ProjectStatus current, ProjectStatus target) {
        if (current == target) {
            throw new BusinessException("Project is already in status: " + current);
        }

        if (target == ProjectStatus.CANCELED) {
            if (current.isTerminal()) {
                throw new BusinessException("Cannot cancel a project that is already " + current);
            }
            return;
        }

        Set<ProjectStatus> allowed = ALLOWED_TRANSITIONS.getOrDefault(current, Set.of());
        if (!allowed.contains(target)) {
            throw new BusinessException(
                    "Invalid status transition from " + current + " to " + target + ". Sequential workflow must be respected.");
        }
    }
}
