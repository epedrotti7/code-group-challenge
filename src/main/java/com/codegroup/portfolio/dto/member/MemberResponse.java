package com.codegroup.portfolio.dto.member;

import com.codegroup.portfolio.domain.enums.MemberAttribution;

public record MemberResponse(
        Long id,
        String name,
        MemberAttribution attribution
) {
}
