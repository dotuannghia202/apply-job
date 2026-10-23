package com.dtn.apply_job.domain.response.application;

import com.dtn.apply_job.util.constant.enums.ApplicationStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ResCreateApplicationDTO {
    UUID id;
    ApplicationStatus status;
    Instant appliedAt;
    UUID interviewSession;
}
