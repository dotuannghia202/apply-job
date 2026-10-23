package com.dtn.apply_job.domain.response.job;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ResGenerateJdDTO {
    String description;
    List<String> requirements;
    List<String> benefits;
}