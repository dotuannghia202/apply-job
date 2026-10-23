package com.dtn.apply_job.domain.request.job;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReqGenerateJdDTO {
    String title;
    String skills;
    String levels;
}
