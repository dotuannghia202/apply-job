package com.dtn.apply_job.mapper;

import com.dtn.apply_job.domain.Specialization;
import com.dtn.apply_job.domain.request.specialization.ReqCreateSpecializationDTO;
import com.dtn.apply_job.domain.response.specialization.ResSpecializationByIndustryId;
import com.dtn.apply_job.domain.response.specialization.ResSpecializationDTO;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SpecializationMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "industry", ignore = true)
    @Mapping(target = "jobs", ignore = true)
    @Mapping(target = "resumes", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    Specialization toSpecialization(ReqCreateSpecializationDTO reqDTO);

    ResSpecializationDTO toResSpecializationDTO(Specialization specialization);

    ResSpecializationByIndustryId toResSpecializationByIndustryId(Specialization specialization);

    List<ResSpecializationDTO> toResSpecializationDTOList(List<Specialization> specializations);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "jobs", ignore = true)
    @Mapping(target = "resumes", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    void updateSpecializationFromEntity(Specialization source, @MappingTarget Specialization target);
}
