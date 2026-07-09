package com.dtn.apply_job.domain.request.application;

import com.dtn.apply_job.util.constant.enums.ApplicationStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class ReqUpdateApplicationStatusDTO {
    @NotNull(message = "Trạng thái không được để trống!")
    private ApplicationStatus status;

    private Instant interviewTime;
    private String interviewLocation;
    private String interviewMessage;
}
