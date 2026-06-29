package com.luccadumas.projecthub.repository;

import com.luccadumas.projecthub.domain.enums.ProjectStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ProjectRepositoryTest {

    @Test
    @DisplayName("Should return empty map when member ids are null or empty")
    void shouldReturnEmptyMapForEmptyMemberIds() {
        ProjectRepository repository = mock(ProjectRepository.class);
        doCallRealMethod().when(repository).countActiveProjectsByMemberIds(any(), any());

        assertThat(repository.countActiveProjectsByMemberIds(null, List.of(ProjectStatus.CANCELED)))
                .isEmpty();
        assertThat(repository.countActiveProjectsByMemberIds(Set.of(), List.of(ProjectStatus.CANCELED)))
                .isEmpty();
    }

    @Test
    @DisplayName("Should map grouped active project counts by member id")
    void shouldMapGroupedActiveProjectCounts() {
        ProjectRepository repository = mock(ProjectRepository.class);
        doCallRealMethod().when(repository).countActiveProjectsByMemberIds(any(), any());
        when(repository.countActiveProjectsGroupedByMemberId(
                Set.of(2L, 3L),
                List.of(ProjectStatus.COMPLETED, ProjectStatus.CANCELED)))
                .thenReturn(List.of(
                        new Object[]{2L, 1L},
                        new Object[]{3L, 2L}
                ));

        Map<Long, Long> counts = repository.countActiveProjectsByMemberIds(
                Set.of(2L, 3L),
                List.of(ProjectStatus.COMPLETED, ProjectStatus.CANCELED));

        assertThat(counts).containsEntry(2L, 1L).containsEntry(3L, 2L);
    }
}
