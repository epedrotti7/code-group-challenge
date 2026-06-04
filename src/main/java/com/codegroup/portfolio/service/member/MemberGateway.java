package com.codegroup.portfolio.service.member;

import com.codegroup.portfolio.dto.member.MemberResponse;

public interface MemberGateway {

    MemberResponse getById(Long id);
}
