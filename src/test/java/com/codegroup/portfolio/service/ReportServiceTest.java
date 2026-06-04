package com.codegroup.portfolio.service;

import com.codegroup.portfolio.domain.entity.Project;
import com.codegroup.portfolio.domain.enums.ProjectStatus;
import com.codegroup.portfolio.dto.report.PortfolioReportResponse;
import com.codegroup.portfolio.repository.ProjectRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @InjectMocks
    private ReportService reportService;

    private static final LocalDate START = LocalDate.of(2026, 1, 1);

    @Test
    void shouldAggregatePortfolioReport() {
        Project closedA = project(ProjectStatus.ENCERRADO, "100000", Set.of(1L, 2L));
        closedA.setActualEndDate(START.plusDays(10));
        Project closedB = project(ProjectStatus.ENCERRADO, "200000", Set.of(2L, 3L));
        closedB.setActualEndDate(START.plusDays(20));
        Project running = project(ProjectStatus.EM_ANDAMENTO, "50000", Set.of(3L, 4L));

        when(projectRepository.findAll()).thenReturn(List.of(closedA, closedB, running));

        PortfolioReportResponse report = reportService.generate();

        assertThat(report.projectCountByStatus().get(ProjectStatus.ENCERRADO)).isEqualTo(2L);
        assertThat(report.projectCountByStatus().get(ProjectStatus.EM_ANDAMENTO)).isEqualTo(1L);
        assertThat(report.projectCountByStatus().get(ProjectStatus.CANCELADO)).isEqualTo(0L);
        assertThat(report.totalBudgetByStatus().get(ProjectStatus.ENCERRADO)).isEqualByComparingTo("300000");
        assertThat(report.totalBudgetByStatus().get(ProjectStatus.EM_ANDAMENTO)).isEqualByComparingTo("50000");
        assertThat(report.averageDurationDaysOfClosedProjects()).isEqualTo(15.0);
        assertThat(report.uniqueAllocatedMembers()).isEqualTo(4L);
    }

    @Test
    void shouldReturnNullAverageWhenNoClosedProjects() {
        when(projectRepository.findAll()).thenReturn(List.of(project(ProjectStatus.EM_ANALISE, "1000", Set.of(1L))));

        PortfolioReportResponse report = reportService.generate();

        assertThat(report.averageDurationDaysOfClosedProjects()).isNull();
        assertThat(report.uniqueAllocatedMembers()).isEqualTo(1L);
    }

    private Project project(ProjectStatus status, String budget, Set<Long> members) {
        return Project.builder()
                .name("Projeto")
                .startDate(START)
                .expectedEndDate(START.plusMonths(2))
                .totalBudget(new BigDecimal(budget))
                .managerId(99L)
                .status(status)
                .memberIds(new java.util.HashSet<>(members))
                .build();
    }
}
