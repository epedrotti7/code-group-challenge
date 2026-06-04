package com.codegroup.portfolio.service;

import com.codegroup.portfolio.domain.entity.Project;
import com.codegroup.portfolio.domain.enums.ProjectStatus;
import com.codegroup.portfolio.dto.report.PortfolioReportResponse;
import com.codegroup.portfolio.repository.ProjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class ReportService {

    private final ProjectRepository projectRepository;

    public ReportService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    @Transactional(readOnly = true)
    public PortfolioReportResponse generate() {
        List<Project> projects = projectRepository.findAll();

        Map<ProjectStatus, Long> countByStatus = new LinkedHashMap<>();
        Map<ProjectStatus, BigDecimal> budgetByStatus = new LinkedHashMap<>();
        for (ProjectStatus status : ProjectStatus.values()) {
            countByStatus.put(status, 0L);
            budgetByStatus.put(status, BigDecimal.ZERO);
        }

        Set<Long> uniqueMembers = new HashSet<>();
        long closedCount = 0;
        long closedDurationSum = 0;

        for (Project project : projects) {
            ProjectStatus status = project.getStatus();
            countByStatus.merge(status, 1L, Long::sum);
            budgetByStatus.merge(status, project.getTotalBudget(), BigDecimal::add);
            uniqueMembers.addAll(project.getMemberIds());

            if (status == ProjectStatus.ENCERRADO && project.getActualEndDate() != null) {
                closedCount++;
                closedDurationSum += ChronoUnit.DAYS.between(project.getStartDate(), project.getActualEndDate());
            }
        }

        Double averageDuration = closedCount == 0 ? null : (double) closedDurationSum / closedCount;

        return new PortfolioReportResponse(
                countByStatus,
                budgetByStatus,
                averageDuration,
                uniqueMembers.size());
    }
}
