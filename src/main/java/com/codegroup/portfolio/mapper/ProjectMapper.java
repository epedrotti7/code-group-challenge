package com.codegroup.portfolio.mapper;

import com.codegroup.portfolio.domain.entity.Project;
import com.codegroup.portfolio.dto.project.ProjectRequest;
import com.codegroup.portfolio.dto.project.ProjectResponse;
import com.codegroup.portfolio.service.RiskCalculator;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class ProjectMapper {

    private final RiskCalculator riskCalculator;

    public ProjectMapper(RiskCalculator riskCalculator) {
        this.riskCalculator = riskCalculator;
    }

    public Project toEntity(ProjectRequest request) {
        Set<Long> members = request.memberIds() == null ? new HashSet<>() : new HashSet<>(request.memberIds());
        return Project.builder()
                .name(request.name())
                .startDate(request.startDate())
                .expectedEndDate(request.expectedEndDate())
                .totalBudget(request.totalBudget())
                .description(request.description())
                .managerId(request.managerId())
                .memberIds(members)
                .build();
    }

    public ProjectResponse toResponse(Project project) {
        return new ProjectResponse(
                project.getId(),
                project.getName(),
                project.getStartDate(),
                project.getExpectedEndDate(),
                project.getActualEndDate(),
                project.getTotalBudget(),
                project.getDescription(),
                project.getManagerId(),
                project.getStatus(),
                riskCalculator.calculate(project),
                project.getMemberIds());
    }
}
