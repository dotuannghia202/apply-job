package com.dtn.apply_job.domain.request.application;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.UUID;


@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReqCreateApplicationDTO {
    @NotNull(message = "Missing job ID information")
    UUID jobId;

    @NotNull(message = "Please select a CV to apply (Resume ID)")
    UUID resumeId;

    String coverLetter;
}
