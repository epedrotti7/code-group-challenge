package com.codegroup.portfolio.service;

import com.codegroup.portfolio.domain.entity.Project;
import com.codegroup.portfolio.domain.enums.ProjectStatus;
import com.codegroup.portfolio.dto.member.MemberResponse;
import com.codegroup.portfolio.dto.project.ProjectFilter;
import com.codegroup.portfolio.dto.project.ProjectRequest;
import com.codegroup.portfolio.dto.project.ProjectResponse;
import com.codegroup.portfolio.dto.project.ProjectUpdateRequest;
import com.codegroup.portfolio.exception.BusinessException;
import com.codegroup.portfolio.exception.ResourceNotFoundException;
import com.codegroup.portfolio.mapper.ProjectMapper;
import com.codegroup.portfolio.repository.ProjectRepository;
import com.codegroup.portfolio.repository.ProjectSpecifications;
import com.codegroup.portfolio.service.member.MemberGateway;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Service
public class ProjectService {

    private static final int MIN_MEMBERS = 1;
    private static final int MAX_MEMBERS = 10;
    private static final int MAX_ACTIVE_ALLOCATIONS = 3;
    private static final List<ProjectStatus> INACTIVE_STATUSES =
            List.of(ProjectStatus.ENCERRADO, ProjectStatus.CANCELADO);

    private final ProjectRepository projectRepository;
    private final ProjectMapper projectMapper;
    private final MemberGateway memberGateway;
    private final StatusTransitionValidator statusTransitionValidator;

    public ProjectService(ProjectRepository projectRepository,
                          ProjectMapper projectMapper,
                          MemberGateway memberGateway,
                          StatusTransitionValidator statusTransitionValidator) {
        this.projectRepository = projectRepository;
        this.projectMapper = projectMapper;
        this.memberGateway = memberGateway;
        this.statusTransitionValidator = statusTransitionValidator;
    }

    @Transactional
    public ProjectResponse create(ProjectRequest request) {
        validateDates(request.startDate(), request.expectedEndDate());
        requireExistingMember(request.managerId());

        Project project = projectMapper.toEntity(request);
        project.setStatus(ProjectStatus.EM_ANALISE);

        Set<Long> members = project.getMemberIds();
        validateMemberCount(members.size());
        for (Long memberId : members) {
            validateAllocatable(memberId);
        }
        return projectMapper.toResponse(projectRepository.save(project));
    }

    @Transactional(readOnly = true)
    public ProjectResponse getById(Long id) {
        return projectMapper.toResponse(findEntity(id));
    }

    @Transactional(readOnly = true)
    public Page<ProjectResponse> list(ProjectFilter filter, Pageable pageable) {
        Specification<Project> spec = Specification
                .allOf(
                        ProjectSpecifications.nameContains(filter.name()),
                        ProjectSpecifications.hasStatus(filter.status()),
                        ProjectSpecifications.hasManager(filter.managerId()),
                        ProjectSpecifications.startsFrom(filter.startFrom()),
                        ProjectSpecifications.startsUntil(filter.startUntil()),
                        ProjectSpecifications.budgetGreaterOrEqual(filter.minBudget()),
                        ProjectSpecifications.budgetLessOrEqual(filter.maxBudget()));
        return projectRepository.findAll(spec, pageable).map(projectMapper::toResponse);
    }

    @Transactional
    public ProjectResponse update(Long id, ProjectUpdateRequest request) {
        validateDates(request.startDate(), request.expectedEndDate());
        requireExistingMember(request.managerId());

        Project project = findEntity(id);
        project.setName(request.name());
        project.setStartDate(request.startDate());
        project.setExpectedEndDate(request.expectedEndDate());
        project.setActualEndDate(request.actualEndDate());
        project.setTotalBudget(request.totalBudget());
        project.setDescription(request.description());
        project.setManagerId(request.managerId());
        return projectMapper.toResponse(projectRepository.save(project));
    }

    @Transactional
    public ProjectResponse changeStatus(Long id, ProjectStatus target) {
        Project project = findEntity(id);
        statusTransitionValidator.validate(project.getStatus(), target);
        project.setStatus(target);
        if (target == ProjectStatus.ENCERRADO && project.getActualEndDate() == null) {
            project.setActualEndDate(LocalDate.now());
        }
        return projectMapper.toResponse(projectRepository.save(project));
    }

    @Transactional
    public void delete(Long id) {
        Project project = findEntity(id);
        if (ProjectStatus.NON_DELETABLE.contains(project.getStatus())) {
            throw new BusinessException("Projetos com status '" + project.getStatus().getLabel()
                    + "' nao podem ser excluidos");
        }
        projectRepository.delete(project);
    }

    @Transactional
    public ProjectResponse allocateMember(Long projectId, Long memberId) {
        Project project = findEntity(projectId);
        if (project.getStatus().isTerminal()) {
            throw new BusinessException("Nao e possivel alocar membros em um projeto com status '"
                    + project.getStatus().getLabel() + "'");
        }
        if (project.getMemberIds().contains(memberId)) {
            throw new BusinessException("O membro ja esta alocado neste projeto");
        }
        if (project.getMemberIds().size() >= MAX_MEMBERS) {
            throw new BusinessException("O projeto ja atingiu o limite de " + MAX_MEMBERS + " membros");
        }
        validateAllocatable(memberId);
        project.getMemberIds().add(memberId);
        return projectMapper.toResponse(projectRepository.save(project));
    }

    @Transactional
    public ProjectResponse removeMember(Long projectId, Long memberId) {
        Project project = findEntity(projectId);
        if (!project.getMemberIds().contains(memberId)) {
            throw new BusinessException("O membro nao esta alocado neste projeto");
        }
        if (project.getMemberIds().size() <= MIN_MEMBERS) {
            throw new BusinessException("O projeto deve manter no minimo " + MIN_MEMBERS + " membro alocado");
        }
        project.getMemberIds().remove(memberId);
        return projectMapper.toResponse(projectRepository.save(project));
    }

    private Project findEntity(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Projeto", id));
    }

    private void validateDates(LocalDate start, LocalDate expectedEnd) {
        if (start != null && expectedEnd != null && expectedEnd.isBefore(start)) {
            throw new BusinessException("A previsao de termino nao pode ser anterior a data de inicio");
        }
    }

    private void validateMemberCount(int size) {
        if (size < MIN_MEMBERS) {
            throw new BusinessException("O projeto deve ter no minimo " + MIN_MEMBERS + " membro alocado");
        }
        if (size > MAX_MEMBERS) {
            throw new BusinessException("O projeto deve ter no maximo " + MAX_MEMBERS + " membros alocados");
        }
    }

    private MemberResponse requireExistingMember(Long memberId) {
        return memberGateway.getById(memberId);
    }

    private void validateAllocatable(Long memberId) {
        MemberResponse member = memberGateway.getById(memberId);
        if (member.attribution() == null || !"FUNCIONARIO".equalsIgnoreCase(member.attribution().name())) {
            throw new BusinessException("Apenas membros com atribuicao 'funcionario' podem ser alocados (membro id "
                    + memberId + ")");
        }
        long activeAllocations = projectRepository.countActiveAllocations(memberId, INACTIVE_STATUSES);
        if (activeAllocations >= MAX_ACTIVE_ALLOCATIONS) {
            throw new BusinessException("O membro id " + memberId + " ja esta alocado em "
                    + MAX_ACTIVE_ALLOCATIONS + " projetos ativos simultaneamente");
        }
    }
}
