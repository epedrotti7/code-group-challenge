package com.codegroup.portfolio.controller;

import com.codegroup.portfolio.dto.member.MemberRequest;
import com.codegroup.portfolio.dto.member.MemberResponse;
import com.codegroup.portfolio.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Membros (API externa mockada)",
        description = "Cadastro e consulta de membros enviando nome e atribuicao (cargo)")
@RestController
@RequestMapping("/api/v1/external/members")
public class MemberMockController {

    private final MemberService memberService;

    public MemberMockController(MemberService memberService) {
        this.memberService = memberService;
    }

    @Operation(summary = "Cria um membro")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MemberResponse create(@Valid @RequestBody MemberRequest request) {
        return memberService.create(request);
    }

    @Operation(summary = "Consulta um membro pelo id")
    @GetMapping("/{id}")
    public MemberResponse getById(@PathVariable Long id) {
        return memberService.getById(id);
    }
}
