package com.dtn.apply_job.domain.response.resume;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
public class ResUpdateResumeDTO {
    private long id;
    private String fileName;

    // Link Cloudinary để Frontend render hoặc cho người dùng tải lại
    private String fileUrl;

    private boolean active;


    private Instant createdAt;


    private Instant updatedAt;


    private String updatedBy;


    private List<String> skills;


    private CandidateInfo candidate;


    private SpecializationInfo specialization;

    // ================= INNER CLASSES (LÀM GỌN OBJECT) =================

    @Getter
    @Setter
    public static class CandidateInfo {
        private long id;
        private String name;
        private String email;
    }

    @Getter
    @Setter
    public static class SpecializationInfo {
        private long id;
        private String name;
    }
}