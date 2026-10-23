package com.dtn.apply_job.domain.response.resume;

import com.dtn.apply_job.util.LanguageUtil;
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
public class ResUpdateResumeDTO {
    UUID id;
    String fileName;
    String fileUrl;
    boolean active;

    Instant createdAt;
    Instant updatedAt;
    String updatedBy;

    List<String> skills;

    CandidateInfo candidate;

    SpecializationInfo specialization;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class CandidateInfo {
        UUID id;
        String name;
        String email;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class SpecializationInfo {
        UUID id;

        @JsonProperty("name_vi")
        String nameVi;

        @JsonProperty("name_en")
        String nameEn;

        @JsonProperty("name")
        public String getName() {
            return LanguageUtil.resolve(this.nameVi, this.nameEn);
        }

        public void setName(String name) {
            if (this.nameVi == null) this.nameVi = name;
            if (this.nameEn == null) this.nameEn = name;
        }
    }
}