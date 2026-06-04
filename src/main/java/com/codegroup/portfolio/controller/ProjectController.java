package com.codegroup.portfolio.controller;

import com.codegroup.portfolio.domain.enums.ProjectStatus;
import com.codegroup.portfolio.dto.PageResponse;
import com.codegroup.portfolio.dto.project.MemberAllocationRequest;
import com.codegroup.portfolio.dto.project.ProjectFilter;
import com.codegroup.portfolio.dto.project.ProjectRequest;
import com.codegroup.portfolio.dto.project.ProjectResponse;
import com.codegroup.portfolio.dto.project.ProjectUpdateRequest;
import com.codegroup.portfolio.dto.project.StatusUpdateRequest;
import com.codegroup.portfolio.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;

@Tag(name = "Projetos", description = "CRUD de projetos, transicao de status e alocacao de membros")
@RestController
@RequestMapping("/api/v1/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @Operation(summary = "Cria um projeto")
    @PostMapping
    public ResponseEntity<ProjectResponse> create(@Valid @RequestBody ProjectRequest request) {
        ProjectResponse created = projectService.create(request);
        return ResponseEntity.created(URI.create("/api/v1/projects/" + created.id())).body(created);
    }

    @Operation(summary = "Consulta um projeto pelo id")
    @GetMapping("/{id}")
    public ProjectResponse getById(@PathVariable Long id) {
        return projectService.getById(id);
    }

    @Operation(summary = "Lista projetos com paginacao e filtros")
    @GetMapping
    public PageResponse<ProjectResponse> list(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) ProjectStatus status,
            @RequestParam(required = false) Long managerId,
            @RequestParam(required = false) LocalDate startFrom,
            @RequestParam(required = false) LocalDate startUntil,
            @RequestParam(required = false) BigDecimal minBudget,
            @RequestParam(required = false) BigDecimal maxBudget,
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        ProjectFilter filter = new ProjectFilter(name, status, managerId, startFrom, startUntil, minBudget, maxBudget);
        return PageResponse.from(projectService.list(filter, pageable));
    }

    @Operation(summary = "Atualiza os dados de um projeto")
    @PutMapping("/{id}")
    public ProjectResponse update(@PathVariable Long id, @Valid @RequestBody ProjectUpdateRequest request) {
        return projectService.update(id, request);
    }

    @Operation(summary = "Altera o status de um projeto respeitando a sequencia logica")
    @PatchMapping("/{id}/status")
    public ProjectResponse changeStatus(@PathVariable Long id, @Valid @RequestBody StatusUpdateRequest request) {
        return projectService.changeStatus(id, request.status());
    }

    @Operation(summary = "Aloca um membro funcionario ao projeto")
    @PostMapping("/{id}/members")
    public ProjectResponse allocateMember(@PathVariable Long id, @Valid @RequestBody MemberAllocationRequest request) {
        return projectService.allocateMember(id, request.memberId());
    }

    @Operation(summary = "Remove um membro do projeto")
    @DeleteMapping("/{id}/members/{memberId}")
    public ProjectResponse removeMember(@PathVariable Long id, @PathVariable Long memberId) {
        return projectService.removeMember(id, memberId);
    }
}
