package com.codegroup.portfolio.repository;

import com.codegroup.portfolio.domain.entity.Project;
import com.codegroup.portfolio.domain.enums.ProjectStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;

public interface ProjectRepository extends JpaRepository<Project, Long>, JpaSpecificationExecutor<Project> {

    @Query("select count(p) from Project p join p.memberIds m "
            + "where m = :memberId and p.status not in :excludedStatuses")
    long countActiveAllocations(@Param("memberId") Long memberId,
                                @Param("excludedStatuses") Collection<ProjectStatus> excludedStatuses);
}
