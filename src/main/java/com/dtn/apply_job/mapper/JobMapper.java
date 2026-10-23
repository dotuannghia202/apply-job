package com.dtn.apply_job.mapper;

import com.dtn.apply_job.domain.Job;
import com.dtn.apply_job.domain.Skill;
import com.dtn.apply_job.domain.request.job.ReqCreateJobDTO;
import com.dtn.apply_job.domain.request.job.ReqUpdateJobDTO;
import com.dtn.apply_job.domain.response.job.ResCreateJobDTO;
import com.dtn.apply_job.domain.response.job.ResJobDTO;
import com.dtn.apply_job.domain.response.job.ResUpdateJobDTO;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Mapper(componentModel = "spring")
public interface JobMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "company", ignore = true)
    @Mapping(target = "specialization", ignore = true)
    @Mapping(target = "skills", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "nameVi", expression = "java(reqDTO.getNameVi() != null ? reqDTO.getNameVi() : reqDTO.getName())")
    @Mapping(target = "nameEn", expression = "java(reqDTO.getNameEn() != null ? reqDTO.getNameEn() : reqDTO.getName())")
    @Mapping(target = "descriptionVi", expression = "java(reqDTO.getDescriptionVi() != null ? reqDTO.getDescriptionVi() : reqDTO.getDescription())")
    @Mapping(target = "descriptionEn", expression = "java(reqDTO.getDescriptionEn() != null ? reqDTO.getDescriptionEn() : reqDTO.getDescription())")
    @Mapping(target = "requirementsVi", expression = "java(reqDTO.getRequirementsVi() != null ? reqDTO.getRequirementsVi() : reqDTO.getRequirements())")
    @Mapping(target = "requirementsEn", expression = "java(reqDTO.getRequirementsEn() != null ? reqDTO.getRequirementsEn() : reqDTO.getRequirements())")
    @Mapping(target = "benefitsVi", expression = "java(reqDTO.getBenefitsVi() != null ? reqDTO.getBenefitsVi() : reqDTO.getBenefits())")
    @Mapping(target = "benefitsEn", expression = "java(reqDTO.getBenefitsEn() != null ? reqDTO.getBenefitsEn() : reqDTO.getBenefits())")
    @Mapping(target = "workingHoursVi", expression = "java(reqDTO.getWorkingHoursVi() != null ? reqDTO.getWorkingHoursVi() : reqDTO.getWorkingHours())")
    @Mapping(target = "workingHoursEn", expression = "java(reqDTO.getWorkingHoursEn() != null ? reqDTO.getWorkingHoursEn() : reqDTO.getWorkingHours())")
    Job toJob(ReqCreateJobDTO reqDTO);

    @Mapping(target = "skillsVi", expression = "java(mapSkillsVi(job.getSkills()))")
    @Mapping(target = "skillsEn", expression = "java(mapSkillsEn(job.getSkills()))")
    @Mapping(target = "isSaved", ignore = true)
    @Mapping(target = "isApplied", ignore = true)
    @Mapping(target = "applicantCount", ignore = true)
    ResJobDTO toResJobDTO(Job job);

    @Mapping(target = "skillsVi", expression = "java(mapSkillsVi(job.getSkills()))")
    @Mapping(target = "skillsEn", expression = "java(mapSkillsEn(job.getSkills()))")
    ResCreateJobDTO toResCreateJobDTO(Job job);

    @Mapping(target = "companyName", source = "company.name")
    @Mapping(target = "specializationNameVi", source = "specialization.nameVi")
    @Mapping(target = "specializationNameEn", source = "specialization.nameEn")
    @Mapping(target = "skillsVi", expression = "java(mapSkillsVi(job.getSkills()))")
    @Mapping(target = "skillsEn", expression = "java(mapSkillsEn(job.getSkills()))")
    ResUpdateJobDTO toResUpdateJobDTO(Job job);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "company", ignore = true)
    @Mapping(target = "specialization", ignore = true)
    @Mapping(target = "skills", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    void updateJobFromDTO(ReqUpdateJobDTO dto, @MappingTarget Job job);

    default List<String> mapSkillsVi(List<Skill> skills) {
        if (skills == null) return Collections.emptyList();
        return skills.stream().map(Skill::getNameVi).filter(Objects::nonNull).toList();
    }

    default List<String> mapSkillsEn(List<Skill> skills) {
        if (skills == null) return Collections.emptyList();
        return skills.stream().map(Skill::getNameEn).filter(Objects::nonNull).toList();
    }
}
