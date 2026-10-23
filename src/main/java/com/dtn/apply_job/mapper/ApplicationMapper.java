package com.dtn.apply_job.mapper;

import com.dtn.apply_job.domain.Application;
import com.dtn.apply_job.domain.request.application.ReqCreateApplicationDTO;
import com.dtn.apply_job.domain.response.application.ResApplicationDTO;
import com.dtn.apply_job.domain.response.application.ResUpdateApplicationDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ApplicationMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "job", ignore = true)
    @Mapping(target = "resume", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "matchScore", ignore = true)
    @Mapping(target = "matchedSkills", ignore = true)
    @Mapping(target = "missingSkills", ignore = true)
    @Mapping(target = "interviewTime", ignore = true)
    @Mapping(target = "interviewLocation", ignore = true)
    @Mapping(target = "interviewMessage", ignore = true)
    @Mapping(target = "appliedAt", ignore = true)
    Application toApplication(ReqCreateApplicationDTO reqCreateApplicationDTO);

    @Mapping(target = "hasCoverLetter", expression = "java(app.getCoverLetter() != null && !app.getCoverLetter().trim().isEmpty())")
    @Mapping(target = "job.id", source = "job.id")
    @Mapping(target = "job.nameVi", source = "job.nameVi")
    @Mapping(target = "job.nameEn", source = "job.nameEn")
    @Mapping(target = "job.minSalary", source = "job.minSalary")
    @Mapping(target = "job.maxSalary", source = "job.maxSalary")
    @Mapping(target = "job.companyName", source = "job.company.name")
    @Mapping(target = "job.location", source = "job.location")
    @Mapping(target = "job.companyLogo", source = "job.company.logo")
    @Mapping(target = "resume.id", source = "resume.id")
    @Mapping(target = "resume.fileName", source = "resume.fileName")
    @Mapping(target = "resume.fileUrl", source = "resume.fileUrl")
    @Mapping(target = "candidate.id", source = "resume.candidate.id")
    @Mapping(target = "candidate.name", source = "resume.candidate.name")
    @Mapping(target = "candidate.email", source = "resume.candidate.email")
    @Mapping(target = "candidate.avatarUrl", source = "resume.candidate.avatarUrl")
    @Mapping(target = "interviewSessionId", ignore = true)
    @Mapping(target = "interviewSessionStatus", ignore = true)
    ResApplicationDTO toResApplicationDTO(Application app);

    ResUpdateApplicationDTO toResUpdateApplicationDTO(Application app);
}
