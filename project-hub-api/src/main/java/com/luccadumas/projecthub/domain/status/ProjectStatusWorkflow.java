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
        ALLOWED_TRANSITIONS.put(ProjectStatus.EM_ANALISE, Set.of(ProjectStatus.ANALISE_REALIZADA, ProjectStatus.CANCELADO));
        ALLOWED_TRANSITIONS.put(ProjectStatus.ANALISE_REALIZADA, Set.of(ProjectStatus.ANALISE_APROVADA, ProjectStatus.CANCELADO));
        ALLOWED_TRANSITIONS.put(ProjectStatus.ANALISE_APROVADA, Set.of(ProjectStatus.INICIADO, ProjectStatus.CANCELADO));
        ALLOWED_TRANSITIONS.put(ProjectStatus.INICIADO, Set.of(ProjectStatus.PLANEJADO, ProjectStatus.CANCELADO));
        ALLOWED_TRANSITIONS.put(ProjectStatus.PLANEJADO, Set.of(ProjectStatus.EM_ANDAMENTO, ProjectStatus.CANCELADO));
        ALLOWED_TRANSITIONS.put(ProjectStatus.EM_ANDAMENTO, Set.of(ProjectStatus.ENCERRADO, ProjectStatus.CANCELADO));
        ALLOWED_TRANSITIONS.put(ProjectStatus.ENCERRADO, Set.of());
        ALLOWED_TRANSITIONS.put(ProjectStatus.CANCELADO, Set.of());
    }

    public void validateTransition(ProjectStatus current, ProjectStatus target) {
        if (current == target) {
            throw new BusinessException("Project is already in status: " + current);
        }

        if (target == ProjectStatus.CANCELADO) {
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
