package com.dtn.apply_job.domain.response.skill;

import com.dtn.apply_job.util.LanguageUtil;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ResUpdateDTO {
    @JsonProperty("name_vi")
    String nameVi;

    @JsonProperty("name_en")
    String nameEn;

    Instant updatedAt;
    String updatedBy;

    @JsonProperty("name")
    public String getName() {
        return LanguageUtil.resolve(this.nameVi, this.nameEn);
    }

    public void setName(String name) {
        if (this.nameVi == null) this.nameVi = name;
        if (this.nameEn == null) this.nameEn = name;
    }
}
