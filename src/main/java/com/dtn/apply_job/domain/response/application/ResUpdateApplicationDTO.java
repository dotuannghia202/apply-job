package com.dtn.apply_job.domain.response.application;

import com.dtn.apply_job.util.constant.enums.ApplicationStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ResUpdateApplicationDTO {
    UUID id;
    ApplicationStatus status;
}


