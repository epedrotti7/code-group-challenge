package com.codegroup.portfolio.service;

import com.codegroup.portfolio.domain.entity.Project;
import com.codegroup.portfolio.domain.enums.MemberAttribution;
import com.codegroup.portfolio.domain.enums.ProjectStatus;
import com.codegroup.portfolio.dto.member.MemberResponse;
import com.codegroup.portfolio.dto.project.ProjectFilter;
import com.codegroup.portfolio.dto.project.ProjectRequest;
import com.codegroup.portfolio.dto.project.ProjectResponse;
import com.codegroup.portfolio.dto.project.ProjectUpdateRequest;
import com.codegroup.portfolio.exception.BusinessException;
import com.codegroup.portfolio.exception.InvalidStatusTransitionException;
import com.codegroup.portfolio.exception.ResourceNotFoundException;
import com.codegroup.portfolio.mapper.ProjectMapper;
import com.codegroup.portfolio.repository.ProjectRepository;
import com.codegroup.portfolio.service.member.MemberGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private MemberGateway memberGateway;
    @Spy
    private ProjectMapper projectMapper = new ProjectMapper(new RiskCalculator());
    @Spy
    private StatusTransitionValidator statusTransitionValidator = new StatusTransitionValidator();

    @InjectMocks
    private ProjectService projectService;

    private static final LocalDate START = LocalDate.of(2026, 1, 1);

    @BeforeEach
    void stubSave() {
        lenient().when(projectRepository.save(any(Project.class))).thenAnswer(inv -> {
            Project p = inv.getArgument(0);
            if (p.getId() == null) {
                p.setId(1L);
            }
            return p;
        });
    }

    private MemberResponse funcionario(Long id) {
        return new MemberResponse(id, "Membro " + id, MemberAttribution.FUNCIONARIO);
    }

    private ProjectRequest request(Set<Long> members) {
        return new ProjectRequest("Projeto X", START, START.plusMonths(2),
                new BigDecimal("50000"), "desc", 99L, members);
    }

    @Test
    void shouldCreateProjectSuccessfully() {
        when(memberGateway.getById(anyLong())).thenAnswer(inv -> funcionario(inv.getArgument(0)));
        when(projectRepository.countActiveAllocations(anyLong(), any())).thenReturn(0L);

        ProjectResponse response = projectService.create(request(new LinkedHashSet<>(List.of(2L, 3L))));

        assertThat(response.status()).isEqualTo(ProjectStatus.EM_ANALISE);
        assertThat(response.memberIds()).containsExactlyInAnyOrder(2L, 3L);
        verify(projectRepository).save(any(Project.class));
    }

    @Test
    void shouldRejectCreationWithoutMembers() {
        when(memberGateway.getById(eq(99L))).thenReturn(funcionario(99L));

        assertThatThrownBy(() -> projectService.create(request(new LinkedHashSet<>())))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("minimo");
    }

    @Test
    void shouldRejectCreationWithMoreThanTenMembers() {
        when(memberGateway.getById(anyLong())).thenAnswer(inv -> funcionario(inv.getArgument(0)));
        Set<Long> members = new LinkedHashSet<>();
        for (long i = 1; i <= 11; i++) {
            members.add(i);
        }
        assertThatThrownBy(() -> projectService.create(request(members)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("maximo");
    }

    @Test
    void shouldRejectNonFuncionarioMember() {
        when(memberGateway.getById(eq(99L))).thenReturn(funcionario(99L));
        when(memberGateway.getById(eq(2L)))
                .thenReturn(new MemberResponse(2L, "Estagiario", MemberAttribution.ESTAGIARIO));

        assertThatThrownBy(() -> projectService.create(request(new LinkedHashSet<>(List.of(2L)))))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("funcionario");
    }

    @Test
    void shouldRejectMemberAllocatedInThreeActiveProjects() {
        when(memberGateway.getById(anyLong())).thenAnswer(inv -> funcionario(inv.getArgument(0)));
        when(projectRepository.countActiveAllocations(eq(2L), any())).thenReturn(3L);

        assertThatThrownBy(() -> projectService.create(request(new LinkedHashSet<>(List.of(2L)))))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("simultaneamente");
    }

    @Test
    void shouldRejectExpectedEndBeforeStart() {
        ProjectRequest req = new ProjectRequest("Projeto X", START, START.minusDays(1),
                new BigDecimal("50000"), "desc", 99L, Set.of(2L));
        assertThatThrownBy(() -> projectService.create(req))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("previsao");
    }

    @Test
    void shouldPropagateManagerNotFound() {
        when(memberGateway.getById(eq(99L))).thenThrow(ResourceNotFoundException.of("Membro", 99L));
        assertThatThrownBy(() -> projectService.create(request(Set.of(2L))))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(projectRepository, never()).save(any());
    }

    @Test
    void shouldBlockDeletionWhenStatusIsNonDeletable() {
        Project project = baseProject(ProjectStatus.EM_ANDAMENTO);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        assertThatThrownBy(() -> projectService.delete(1L))
                .isInstanceOf(BusinessException.class);
        verify(projectRepository, never()).delete(any(Project.class));
    }

    @Test
    void shouldDeleteWhenStatusAllows() {
        Project project = baseProject(ProjectStatus.EM_ANALISE);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        projectService.delete(1L);

        verify(projectRepository).delete(project);
    }

    @Test
    void shouldChangeStatusAndSetActualEndDateOnClose() {
        Project project = baseProject(ProjectStatus.EM_ANDAMENTO);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        ProjectResponse response = projectService.changeStatus(1L, ProjectStatus.ENCERRADO);

        assertThat(response.status()).isEqualTo(ProjectStatus.ENCERRADO);
        assertThat(project.getActualEndDate()).isNotNull();
    }

    @Test
    void shouldRejectInvalidStatusTransition() {
        Project project = baseProject(ProjectStatus.EM_ANALISE);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        assertThatThrownBy(() -> projectService.changeStatus(1L, ProjectStatus.ENCERRADO))
                .isInstanceOf(InvalidStatusTransitionException.class);
    }

    @Test
    void shouldAllocateMemberSuccessfully() {
        Project project = baseProject(ProjectStatus.EM_ANALISE);
        project.getMemberIds().add(2L);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(memberGateway.getById(eq(5L))).thenReturn(funcionario(5L));
        when(projectRepository.countActiveAllocations(eq(5L), any())).thenReturn(0L);

        ProjectResponse response = projectService.allocateMember(1L, 5L);

        assertThat(response.memberIds()).contains(5L);
    }

    @Test
    void shouldRejectAllocationWhenProjectIsFull() {
        Project project = baseProject(ProjectStatus.EM_ANALISE);
        for (long i = 1; i <= 10; i++) {
            project.getMemberIds().add(i);
        }
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        assertThatThrownBy(() -> projectService.allocateMember(1L, 99L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("limite");
    }

    @Test
    void shouldRejectAllocationWhenMemberAlreadyAllocated() {
        Project project = baseProject(ProjectStatus.EM_ANALISE);
        project.getMemberIds().add(2L);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        assertThatThrownBy(() -> projectService.allocateMember(1L, 2L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("ja esta alocado");
    }

    @Test
    void shouldRejectAllocationWhenProjectTerminal() {
        Project project = baseProject(ProjectStatus.ENCERRADO);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        assertThatThrownBy(() -> projectService.allocateMember(1L, 2L))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void shouldRemoveMemberSuccessfully() {
        Project project = baseProject(ProjectStatus.EM_ANALISE);
        project.getMemberIds().add(2L);
        project.getMemberIds().add(3L);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        ProjectResponse response = projectService.removeMember(1L, 3L);

        assertThat(response.memberIds()).doesNotContain(3L);
    }

    @Test
    void shouldRejectRemovingLastMember() {
        Project project = baseProject(ProjectStatus.EM_ANALISE);
        project.getMemberIds().add(2L);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        assertThatThrownBy(() -> projectService.removeMember(1L, 2L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("minimo");
    }

    @Test
    void shouldFailGetByIdWhenNotFound() {
        when(projectRepository.findById(42L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> projectService.getById(42L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void shouldUpdateProjectFields() {
        Project project = baseProject(ProjectStatus.EM_ANALISE);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(memberGateway.getById(eq(7L))).thenReturn(funcionario(7L));

        ProjectUpdateRequest req = new ProjectUpdateRequest("Novo Nome", START, START.plusMonths(4),
                null, new BigDecimal("250000"), "nova desc", 7L);

        ProjectResponse response = projectService.update(1L, req);

        assertThat(response.name()).isEqualTo("Novo Nome");
        assertThat(response.managerId()).isEqualTo(7L);
        assertThat(response.totalBudget()).isEqualByComparingTo("250000");
    }

    @Test
    void shouldListProjectsMappingToResponse() {
        Project project = baseProject(ProjectStatus.EM_ANALISE);
        project.getMemberIds().add(2L);
        Page<Project> page = new PageImpl<>(List.of(project));
        when(projectRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        Page<ProjectResponse> result = projectService.list(
                new ProjectFilter(null, null, null, null, null, null, null), PageRequest.of(0, 20));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).id()).isEqualTo(1L);
    }

    private Project baseProject(ProjectStatus status) {
        return Project.builder()
                .id(1L)
                .name("Projeto")
                .startDate(START)
                .expectedEndDate(START.plusMonths(2))
                .totalBudget(new BigDecimal("50000"))
                .managerId(99L)
                .status(status)
                .memberIds(new LinkedHashSet<>())
                .build();
    }
}
