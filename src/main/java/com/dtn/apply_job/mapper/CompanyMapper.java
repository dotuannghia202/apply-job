package com.dtn.apply_job.mapper;

import com.dtn.apply_job.domain.Company;
import com.dtn.apply_job.domain.request.company.ReqCreateCompanyDTO;
import com.dtn.apply_job.domain.request.company.ReqUpdateCompanyDTO;
import com.dtn.apply_job.domain.response.company.ResCompanyDTO;
import com.dtn.apply_job.domain.response.company.ResCreateCompanyDTO;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface CompanyMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "users", ignore = true)
    @Mapping(target = "jobs", ignore = true)
    @Mapping(target = "descriptionVi", expression = "java(reqDTO.getDescriptionVi() != null ? reqDTO.getDescriptionVi() : reqDTO.getDescription())")
    @Mapping(target = "descriptionEn", expression = "java(reqDTO.getDescriptionEn() != null ? reqDTO.getDescriptionEn() : reqDTO.getDescription())")
    Company toCompany(ReqCreateCompanyDTO reqDTO);

    @Mapping(target = "employerName", ignore = true)
    @Mapping(target = "employerEmail", ignore = true)
    ResCompanyDTO toResCompanyDTO(Company company);

    ResCreateCompanyDTO toResCreateCompanyDTO(Company company);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "users", ignore = true)
    @Mapping(target = "jobs", ignore = true)
    void updateCompanyFromDTO(ReqUpdateCompanyDTO reqDTO, @MappingTarget Company company);
}
