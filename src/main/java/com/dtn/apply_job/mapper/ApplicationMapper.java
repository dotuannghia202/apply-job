package com.dtn.apply_job.mapper;

import com.dtn.apply_job.domain.Application;
import com.dtn.apply_job.domain.request.application.ReqCreateApplicationDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ApplicationMapper {
    Application toApplication(ReqCreateApplicationDTO reqCreateApplicationDTO);

}
