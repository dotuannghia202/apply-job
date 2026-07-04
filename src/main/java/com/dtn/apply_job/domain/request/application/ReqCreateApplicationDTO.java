package com.dtn.apply_job.domain.request.application;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ReqCreateApplicationDTO {
    @NotNull(message = "Thiếu thông tin ID công việc")
    private Long jobId;

    @NotNull(message = "Vui lòng chọn CV để ứng tuyển (Resume ID)")
    private Long resumeId;

    private String coverLetter;
}
