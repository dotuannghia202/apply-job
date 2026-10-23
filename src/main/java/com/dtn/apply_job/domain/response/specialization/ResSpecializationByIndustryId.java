package com.dtn.apply_job.domain.response.specialization;

import com.dtn.apply_job.util.LanguageUtil;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ResSpecializationByIndustryId {
    UUID id;

    @JsonProperty("name_vi")
    String nameVi;

    @JsonProperty("name_en")
    String nameEn;

    public ResSpecializationByIndustryId(UUID id, String name) {
        this.id = id;
        this.nameVi = name;
        this.nameEn = name;
    }

    @JsonProperty("name")
    public String getName() {
        return LanguageUtil.resolve(this.nameVi, this.nameEn);
    }
}
