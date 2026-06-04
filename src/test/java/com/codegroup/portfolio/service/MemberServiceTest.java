package com.codegroup.portfolio.service;

import com.codegroup.portfolio.domain.entity.Member;
import com.codegroup.portfolio.domain.enums.MemberAttribution;
import com.codegroup.portfolio.dto.member.MemberRequest;
import com.codegroup.portfolio.dto.member.MemberResponse;
import com.codegroup.portfolio.exception.ResourceNotFoundException;
import com.codegroup.portfolio.mapper.MemberMapper;
import com.codegroup.portfolio.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;
    @Spy
    private MemberMapper memberMapper = new MemberMapper();

    @InjectMocks
    private MemberService memberService;

    @Test
    void shouldCreateMember() {
        when(memberRepository.save(any(Member.class))).thenAnswer(inv -> {
            Member m = inv.getArgument(0);
            m.setId(1L);
            return m;
        });

        MemberResponse response = memberService.create(
                new MemberRequest("Maria", MemberAttribution.FUNCIONARIO));

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.attribution()).isEqualTo(MemberAttribution.FUNCIONARIO);
    }

    @Test
    void shouldGetMemberById() {
        Member member = Member.builder().id(5L).name("Joao").attribution(MemberAttribution.GERENTE).build();
        when(memberRepository.findById(5L)).thenReturn(Optional.of(member));

        MemberResponse response = memberService.getById(5L);

        assertThat(response.name()).isEqualTo("Joao");
    }

    @Test
    void shouldFailWhenMemberNotFound() {
        when(memberRepository.findById(9L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> memberService.getById(9L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
