package com.dtn.apply_job.domain.request.application;

import com.dtn.apply_job.util.constant.enums.ApplicationStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqUpdateApplicationStatusDTO {
    @NotNull(message = "Status isn't be null!")
    private ApplicationStatus status;
}
