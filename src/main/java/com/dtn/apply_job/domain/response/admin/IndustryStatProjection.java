package com.dtn.apply_job.domain.response.admin;

public interface IndustryStatProjection {
    String getIndustryName();

    Long getJobCount(); // Bắt buộc dùng Long (Object) để chứa kết quả của COUNT()
}
