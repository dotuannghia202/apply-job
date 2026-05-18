package com.dtn.apply_job.domain.response.employer;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResHrDashboardStatsDTO {
    private long totalActiveJobs;
    private long totalApplicants;
    private double avgAiMatchRate;
}