package com.dtn.apply_job.domain.response.company;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResCompanyStatsDTO {
    private long totalCompanies;
    private long pendingApproval;
    private long approved;
}