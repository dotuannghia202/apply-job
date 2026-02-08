package com.dtn.apply_job.repository;

import com.dtn.apply_job.domain.Skill;
import com.dtn.apply_job.util.constant.enums.LevelEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface SkillRepository extends JpaRepository<Skill, Long>, JpaSpecificationExecutor<Skill> {
    boolean existsByName(LevelEnum name);

    List<Skill> findByIdIn(List<Long> reqSkills);
}