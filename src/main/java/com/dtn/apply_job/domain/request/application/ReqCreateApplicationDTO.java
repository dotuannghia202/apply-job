package com.dtn.apply_job.domain.request.application;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ReqCreateApplicationDTO {
    @NotNull(message = "Job ID information is missing")
    private Long jobId;

    @NotNull(message = "Please select a CV to apply for (Resume ID)")
    private Long resumeId;

    // Cover letter là tùy chọn, không cần @NotNull
    private String coverLetter;
}
