package com.dtn.apply_job.domain.request.resume;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ReqUpdateResumeDTO {


    @Size(min = 1, message = "File name must not blank!")
    private String fileName;

    @Size(min = 1, message = "File path must not blank!")
    private String fileUrl;

    
    private Boolean isActive;

    private Long specializationId;

    private List<Long> skillIds;
}