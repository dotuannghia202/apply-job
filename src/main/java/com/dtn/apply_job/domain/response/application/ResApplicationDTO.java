package com.dtn.apply_job.domain.response.application;

import com.dtn.apply_job.util.constant.enums.ApplicationStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class ResApplicationDTO {
    private Long id;
    private ApplicationStatus status;
    private Double matchScore; // Điểm AI
    private String coverLetter;


    private Instant appliedAt;

    private JobInfo job;
    private ResumeInfo resume;
    private CandidateInfo candidate; // Phẳng hóa dữ liệu để FE dễ dùng

    // --- INNER CLASSES (GỌN NHẸ) ---
    @Getter
    @Setter
    public static class JobInfo {
        private Long id;
        private String name;
        private String companyName;
    }

    @Getter
    @Setter
    public static class ResumeInfo {
        private Long id;
        private String fileName;
        private String fileUrl; // HR dùng link này để xem CV
    }

    @Getter
    @Setter
    public static class CandidateInfo {
        private Long id;
        private String name;
        private String email;
    }
}
