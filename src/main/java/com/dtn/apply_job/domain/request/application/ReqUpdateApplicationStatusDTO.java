package com.dtn.apply_job.domain.request.application;

import com.dtn.apply_job.util.constant.enums.ApplicationStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReqUpdateApplicationStatusDTO {
    @NotNull(message = "Trạng thái không được để trống!")
    ApplicationStatus status;

    Instant interviewTime;
    String interviewLocation;
    String interviewMessage;
}
