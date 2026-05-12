package com.dtn.apply_job.domain.response.application;

import com.dtn.apply_job.util.constant.enums.ApplicationStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class ResCreateApplicationDTO {
    private Long id;
    private ApplicationStatus status;
    private Instant appliedAt;
}
