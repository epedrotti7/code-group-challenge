package com.codegroup.portfolio.repository;

import com.codegroup.portfolio.domain.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {
}
