package com.dtn.apply_job.domain.response.resume;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
public class ResResumeDTO {
    private long id;
    private String fileName;
    private String fileUrl;
    private boolean active;

    @JsonProperty("isDefault")
    private boolean isDefault;


    private Instant createdAt;


    private Instant updatedAt;


    private List<String> skills;


    private CandidateInfo candidate;


    private SpecializationInfo specialization;


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