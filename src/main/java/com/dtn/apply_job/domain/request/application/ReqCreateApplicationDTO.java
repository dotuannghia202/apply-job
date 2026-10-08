package com.dtn.apply_job.domain.request.application;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;


@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReqCreateApplicationDTO {
    @NotNull(message = "Missing job ID information")
    private Long jobId;

    @NotNull(message = "Please select a CV to apply (Resume ID)")
    private Long resumeId;

    private String coverLetter;
}
