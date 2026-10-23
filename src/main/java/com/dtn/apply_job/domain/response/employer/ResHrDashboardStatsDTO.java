package com.dtn.apply_job.domain.response.employer;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ResHrDashboardStatsDTO {
    long totalActiveJobs;
    long totalApplicants;
    double avgAiMatchRate;
}