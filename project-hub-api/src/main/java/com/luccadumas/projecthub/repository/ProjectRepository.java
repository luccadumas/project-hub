package com.luccadumas.projecthub.repository;

import com.luccadumas.projecthub.domain.enums.ProjectStatus;
import com.luccadumas.projecthub.entity.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public interface ProjectRepository extends JpaRepository<Project, Long>, JpaSpecificationExecutor<Project> {

    @EntityGraph(attributePaths = "members")
    @Override
    Page<Project> findAll(Specification<Project> spec, Pageable pageable);

    @EntityGraph(attributePaths = "members")
    Optional<Project> findWithMembersById(Long id);

    @Query("""
            SELECT COUNT(DISTINCT p) FROM Project p
            JOIN p.members m
            WHERE m.id = :memberId
            AND p.status NOT IN :inactiveStatuses
            """)
    long countActiveProjectsByMemberId(
            @Param("memberId") Long memberId,
            @Param("inactiveStatuses") List<ProjectStatus> inactiveStatuses);

    @Query("""
            SELECT m.id, COUNT(DISTINCT p) FROM Project p
            JOIN p.members m
            WHERE m.id IN :memberIds
            AND p.status NOT IN :inactiveStatuses
            GROUP BY m.id
            """)
    List<Object[]> countActiveProjectsGroupedByMemberId(
            @Param("memberIds") Set<Long> memberIds,
            @Param("inactiveStatuses") List<ProjectStatus> inactiveStatuses);

    default Map<Long, Long> countActiveProjectsByMemberIds(
            Set<Long> memberIds,
            List<ProjectStatus> inactiveStatuses) {
        if (memberIds == null || memberIds.isEmpty()) {
            return Map.of();
        }
        return countActiveProjectsGroupedByMemberId(memberIds, inactiveStatuses).stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (Long) row[1]
                ));
    }

    @Query("SELECT COUNT(DISTINCT m.id) FROM Project p JOIN p.members m")
    long countDistinctAllocatedMembers();

    @Query("""
            SELECT p.status, COUNT(p), COALESCE(SUM(p.totalBudget), 0)
            FROM Project p
            GROUP BY p.status
            """)
    List<Object[]> aggregateProjectsByStatus();

    @Query(value = """
            SELECT AVG((actual_end_date - start_date)::double precision)
            FROM projects
            WHERE status = 'COMPLETED' AND actual_end_date IS NOT NULL
            """, nativeQuery = true)
    Double averageClosedProjectDurationDays();
}
