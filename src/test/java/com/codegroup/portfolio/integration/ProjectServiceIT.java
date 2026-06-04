package com.codegroup.portfolio.integration;

import com.codegroup.portfolio.domain.enums.MemberAttribution;
import com.codegroup.portfolio.domain.enums.ProjectStatus;
import com.codegroup.portfolio.dto.member.MemberResponse;
import com.codegroup.portfolio.dto.project.ProjectFilter;
import com.codegroup.portfolio.dto.project.ProjectRequest;
import com.codegroup.portfolio.dto.project.ProjectResponse;
import com.codegroup.portfolio.exception.BusinessException;
import com.codegroup.portfolio.repository.ProjectRepository;
import com.codegroup.portfolio.service.ProjectService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

class ProjectServiceIT extends AbstractPostgresIT {

    private static final LocalDate START = LocalDate.of(2026, 1, 1);
    private static final Long FUNCIONARIO_ID = 100L;

    @Autowired
    private ProjectService projectService;
    @Autowired
    private ProjectRepository projectRepository;
    @MockBean
    private com.codegroup.portfolio.service.member.MemberGateway memberGateway;

    @BeforeEach
    void setUp() {
        projectRepository.deleteAll();
        when(memberGateway.getById(anyLong()))
                .thenAnswer(inv -> new MemberResponse(inv.getArgument(0), "Membro", MemberAttribution.FUNCIONARIO));
    }

    private ProjectRequest request(String name) {
        return new ProjectRequest(name, START, START.plusMonths(2),
                new BigDecimal("80000"), "desc", 1L, Set.of(FUNCIONARIO_ID));
    }

    @Test
    void persistsProjectAndCalculatesRiskAgainstRealDatabase() {
        ProjectResponse response = projectService.create(request("Projeto A"));

        assertThat(response.id()).isNotNull();
        assertThat(response.status()).isEqualTo(ProjectStatus.EM_ANALISE);
        assertThat(projectRepository.findById(response.id())).isPresent();
    }

    @Test
    void enforcesThreeActiveAllocationLimitUsingRealJpqlQuery() {
        projectService.create(request("Projeto 1"));
        projectService.create(request("Projeto 2"));
        projectService.create(request("Projeto 3"));

        assertThatThrownBy(() -> projectService.create(request("Projeto 4")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("simultaneamente");
    }

    @Test
    void filtersAndPaginatesPersistedProjects() {
        projectService.create(request("Alpha Datacenter"));
        projectService.create(request("Beta Mobile"));

        Page<ProjectResponse> page = projectService.list(
                new ProjectFilter("alpha", null, null, null, null, null, null),
                PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent().get(0).name()).isEqualTo("Alpha Datacenter");
    }
}
