package com.codegroup.portfolio.service;

import com.codegroup.portfolio.domain.entity.Member;
import com.codegroup.portfolio.dto.member.MemberRequest;
import com.codegroup.portfolio.dto.member.MemberResponse;
import com.codegroup.portfolio.exception.ResourceNotFoundException;
import com.codegroup.portfolio.mapper.MemberMapper;
import com.codegroup.portfolio.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final MemberMapper memberMapper;

    public MemberService(MemberRepository memberRepository, MemberMapper memberMapper) {
        this.memberRepository = memberRepository;
        this.memberMapper = memberMapper;
    }

    @Transactional
    public MemberResponse create(MemberRequest request) {
        Member saved = memberRepository.save(memberMapper.toEntity(request));
        return memberMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public MemberResponse getById(Long id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Membro", id));
        return memberMapper.toResponse(member);
    }

    @Transactional(readOnly = true)
    public List<MemberResponse> findAll() {
        return memberRepository.findAll().stream()
                .map(memberMapper::toResponse)
                .toList();
    }
}
