package com.dtn.apply_job.domain.response.admin;

import com.dtn.apply_job.util.LanguageUtil;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ResAdminDashboardDTO {
    // 1. Dữ liệu cho Top Cards
    long totalUsers;
    long totalCandidates;
    long totalEmployers;
    long totalCompanies;
    long totalActiveJobs;
    long totalApplications;

    // 2. Dữ liệu cho Biểu đồ tròn (Tùy chọn nâng cao)
    List<IndustryStat> industryStats;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class IndustryStat {
        @JsonProperty("industry_name_vi")
        String industryNameVi;

        @JsonProperty("industry_name_en")
        String industryNameEn;

        long jobCount;

        public IndustryStat(String industryName, long jobCount) {
            this.industryNameVi = industryName;
            this.industryNameEn = industryName;
            this.jobCount = jobCount;
        }

        @JsonProperty("industry_name")
        public String getIndustryName() {
            return LanguageUtil.resolve(this.industryNameVi, this.industryNameEn);
        }
    }
}
