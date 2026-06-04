package com.codegroup.portfolio.repository;

import com.codegroup.portfolio.domain.entity.Project;
import com.codegroup.portfolio.domain.enums.ProjectStatus;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;

public final class ProjectSpecifications {

    private ProjectSpecifications() {
    }

    public static Specification<Project> nameContains(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        String pattern = "%" + name.toLowerCase() + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get("name")), pattern);
    }

    public static Specification<Project> hasStatus(ProjectStatus status) {
        if (status == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<Project> hasManager(Long managerId) {
        if (managerId == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("managerId"), managerId);
    }

    public static Specification<Project> startsFrom(LocalDate from) {
        if (from == null) {
            return null;
        }
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("startDate"), from);
    }

    public static Specification<Project> startsUntil(LocalDate to) {
        if (to == null) {
            return null;
        }
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("startDate"), to);
    }

    public static Specification<Project> budgetGreaterOrEqual(BigDecimal min) {
        if (min == null) {
            return null;
        }
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("totalBudget"), min);
    }

    public static Specification<Project> budgetLessOrEqual(BigDecimal max) {
        if (max == null) {
            return null;
        }
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("totalBudget"), max);
    }
}
