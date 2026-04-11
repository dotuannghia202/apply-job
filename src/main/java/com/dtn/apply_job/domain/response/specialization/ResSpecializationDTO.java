package com.dtn.apply_job.domain.response.specialization;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class ResSpecializationDTO {
    private long id;
    private String name;
    private Instant createdAt;
    private String createdBy;

    
    private IndustryInfo industry;

    @Getter
    @Setter
    public static class IndustryInfo {
        private long id;
        private String name;
    }
}