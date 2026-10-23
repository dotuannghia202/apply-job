package com.dtn.apply_job.repository;

import com.dtn.apply_job.domain.Skill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface SkillRepository extends JpaRepository<Skill, UUID>, JpaSpecificationExecutor<Skill> {
    boolean existsByNameVi(String nameVi);
    boolean existsByNameEn(String nameEn);

    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM Skill s WHERE LOWER(s.nameVi) = LOWER(:name) OR LOWER(s.nameEn) = LOWER(:name)")
    boolean existsByName(@Param("name") String name);

    List<Skill> findByIdIn(List<UUID> reqSkills);
}