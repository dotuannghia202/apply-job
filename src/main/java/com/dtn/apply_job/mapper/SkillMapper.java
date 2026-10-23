package com.dtn.apply_job.mapper;

import com.dtn.apply_job.domain.Skill;
import com.dtn.apply_job.domain.response.skill.ResUpdateDTO;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface SkillMapper {

    ResUpdateDTO toResUpdateDTO(Skill skill);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "jobs", ignore = true)
    @Mapping(target = "resumes", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    void updateSkillFromEntity(Skill source, @MappingTarget Skill target);
}
