package com.dtn.apply_job.domain.request.application;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReqUpdateAppByCandidateDTO {
    UUID resumeId;
    String coverLetter;
}
