package com.dtn.apply_job.domain.request.application;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqUpdateAppByCandidateDTO {
    private Long resumeId;
    private String coverLetter;
}
