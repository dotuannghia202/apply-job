package com.dtn.apply_job.domain.response.admin;

import com.dtn.apply_job.util.LanguageUtil;

public interface IndustryStatProjection {
    String getIndustryNameVi();
    String getIndustryNameEn();

    default String getIndustryName() {
        return LanguageUtil.resolve(getIndustryNameVi(), getIndustryNameEn());
    }

    Long getJobCount();
}
