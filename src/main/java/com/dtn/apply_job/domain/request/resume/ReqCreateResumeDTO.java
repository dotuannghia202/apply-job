package com.dtn.apply_job.domain.request.resume;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ReqCreateResumeDTO {

    @NotBlank(message = "File name must not be blank")
    private String fileName;

    @NotBlank(message = "URL file must not be blank")
    private String fileUrl;


    private Long specializationId;


    private List<Long> skillIds;

    
}