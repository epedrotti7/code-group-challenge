package com.codegroup.portfolio.mapper;

import com.codegroup.portfolio.domain.entity.Member;
import com.codegroup.portfolio.dto.member.MemberRequest;
import com.codegroup.portfolio.dto.member.MemberResponse;
import org.springframework.stereotype.Component;

@Component
public class MemberMapper {

    public Member toEntity(MemberRequest request) {
        return Member.builder()
                .name(request.name())
                .attribution(request.attribution())
                .build();
    }

    public MemberResponse toResponse(Member member) {
        return new MemberResponse(member.getId(), member.getName(), member.getAttribution());
    }
}
