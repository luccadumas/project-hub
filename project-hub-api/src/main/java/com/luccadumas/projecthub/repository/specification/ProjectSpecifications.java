package com.luccadumas.projecthub.repository.specification;

import com.luccadumas.projecthub.domain.enums.ProjectStatus;
import com.luccadumas.projecthub.domain.enums.RiskLevel;
import com.luccadumas.projecthub.entity.Project;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public final class ProjectSpecifications {

    private ProjectSpecifications() {
    }

    public static Specification<Project> withFilters(
            String name,
            ProjectStatus status,
            RiskLevel riskLevel,
            Long managerId,
            LocalDate startDateFrom,
            LocalDate startDateTo,
            LocalDate expectedEndDateFrom,
            LocalDate expectedEndDateTo) {

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (name != null && !name.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
            }

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            if (managerId != null) {
                predicates.add(cb.equal(root.get("managerId"), managerId));
            }

            if (startDateFrom != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("startDate"), startDateFrom));
            }

            if (startDateTo != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("startDate"), startDateTo));
            }

            if (expectedEndDateFrom != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("expectedEndDate"), expectedEndDateFrom));
            }

            if (expectedEndDateTo != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("expectedEndDate"), expectedEndDateTo));
            }

            if (riskLevel != null) {
                predicates.add(cb.equal(root.get("riskLevel"), riskLevel));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
