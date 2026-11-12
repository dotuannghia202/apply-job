package com.dtn.apply_job.domain.response.specialization;

import com.dtn.apply_job.util.LanguageUtil;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ResSpecializationDTO {
    UUID id;

    @JsonProperty(value = "name_vi", access = JsonProperty.Access.WRITE_ONLY)
    String nameVi;

    @JsonProperty(value = "name_en", access = JsonProperty.Access.WRITE_ONLY)
    String nameEn;

    Instant createdAt;
    String createdBy;

    IndustryInfo industry;

    @JsonProperty("name")
    public String getName() {
        return LanguageUtil.resolve(this.nameVi, this.nameEn);
    }

    public void setName(String name) {
        if (this.nameVi == null) this.nameVi = name;
        if (this.nameEn == null) this.nameEn = name;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class IndustryInfo {
        UUID id;

        @JsonProperty(value = "name_vi", access = JsonProperty.Access.WRITE_ONLY)
        String nameVi;

        @JsonProperty(value = "name_en", access = JsonProperty.Access.WRITE_ONLY)
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