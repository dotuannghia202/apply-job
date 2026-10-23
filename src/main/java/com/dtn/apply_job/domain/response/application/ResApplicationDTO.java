package com.dtn.apply_job.domain.response.application;

import com.dtn.apply_job.util.LanguageUtil;
import com.dtn.apply_job.util.constant.enums.ApplicationStatus;
import com.dtn.apply_job.util.constant.enums.InterviewSessionStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ResApplicationDTO {
    UUID id;
    ApplicationStatus status;
    Double matchScore; // Điểm AI
    List<String> matchedSkills;
    List<String> missingSkills;
    String coverLetter;
    Boolean hasCoverLetter;

    Instant appliedAt;
    Instant interviewTime;
    String interviewLocation;
    String interviewMessage;

    // AI Online Interview Session info
    UUID interviewSessionId;
    InterviewSessionStatus interviewSessionStatus;

    JobInfo job;
    ResumeInfo resume;
    CandidateInfo candidate; // Phẳng hóa dữ liệu để FE dễ dùng

    // --- INNER CLASSES (GỌN NHẸ) ---
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class JobInfo {
        UUID id;

        @JsonProperty("name_vi")
        String nameVi;

        @JsonProperty("name_en")
        String nameEn;

        Double minSalary;
        Double maxSalary;
        String companyName;
        String location;
        String companyLogo;

        @JsonProperty("name")
        public String getName() {
            return LanguageUtil.resolve(this.nameVi, this.nameEn);
        }

        public void setName(String name) {
            if (this.nameVi == null) this.nameVi = name;
            if (this.nameEn == null) this.nameEn = name;
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class ResumeInfo {
        UUID id;
        String fileName;
        String fileUrl; // HR dùng link này để xem CV
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class CandidateInfo {
        UUID id;
        String name;
        String email;
        String avatarUrl;
    }
}
