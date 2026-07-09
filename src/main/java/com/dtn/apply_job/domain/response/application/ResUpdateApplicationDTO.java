package com.dtn.apply_job.domain.response.application;

import com.dtn.apply_job.util.constant.enums.ApplicationStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResUpdateApplicationDTO {
    private Long id;
    private ApplicationStatus status;


}


