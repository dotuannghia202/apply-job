package com.dtn.apply_job.mapper;

import com.dtn.apply_job.domain.Resume;
import com.dtn.apply_job.domain.Skill;
import com.dtn.apply_job.domain.request.resume.ReqCreateResumeDTO;
import com.dtn.apply_job.domain.request.resume.ReqUpdateResumeDTO;
import com.dtn.apply_job.domain.response.resume.ResResumeDTO;
import com.dtn.apply_job.domain.response.resume.ResUpdateResumeDTO;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.Collections;
import java.util.List;

@Mapper(componentModel = "spring")
public interface ResumeMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "candidate", ignore = true)
    @Mapping(target = "specialization", ignore = true)
    @Mapping(target = "skills", ignore = true)
    @Mapping(target = "parsedText", ignore = true)
    @Mapping(target = "active", constant = "true")
    @Mapping(target = "isDefault", constant = "false")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    Resume toResume(ReqCreateResumeDTO reqDTO);

    @Mapping(target = "skills", expression = "java(mapSkillNames(resume.getSkills()))")
    @Mapping(target = "isDefault", source = "default")
    ResResumeDTO toResResumeDTO(Resume resume);

    @Mapping(target = "skills", expression = "java(mapSkillNames(resume.getSkills()))")
    ResUpdateResumeDTO toResUpdateResumeDTO(Resume resume);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "candidate", ignore = true)
    @Mapping(target = "specialization", ignore = true)
    @Mapping(target = "skills", ignore = true)
    @Mapping(target = "parsedText", ignore = true)
    @Mapping(target = "default", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    void updateResumeFromDTO(ReqUpdateResumeDTO dto, @MappingTarget Resume resume);

    default List<String> mapSkillNames(List<Skill> skills) {
        if (skills == null) return Collections.emptyList();
        return skills.stream().map(Skill::getName).toList();
    }
}
