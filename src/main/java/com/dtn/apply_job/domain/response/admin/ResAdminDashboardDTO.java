package com.dtn.apply_job.domain.response.admin;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ResAdminDashboardDTO {
    // 1. Dữ liệu cho Top Cards
    private long totalUsers;
    private long totalCandidates;
    private long totalEmployers;
    private long totalCompanies;
    private long totalActiveJobs;
    private long totalApplications;

    // 2. Dữ liệu cho Biểu đồ tròn (Tùy chọn nâng cao)
    private List<IndustryStat> industryStats;

    @Getter
    @Setter
    @AllArgsConstructor
    public static class IndustryStat {
        private String industryName;
        private long jobCount;
    }
}
