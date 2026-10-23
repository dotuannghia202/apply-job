package com.dtn.apply_job.domain.response.company;

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
public class ResCreateCompanyDTO {
    UUID id;
    String name;

    @JsonProperty("description_vi")
    String descriptionVi;

    @JsonProperty("description_en")
    String descriptionEn;

    String address;
    String logo;
    Instant createdAt;
    String createdBy;

    @JsonProperty("description")
    public String getDescription() {
        return LanguageUtil.resolve(this.descriptionVi, this.descriptionEn);
    }

    public void setDescription(String description) {
        if (this.descriptionVi == null) this.descriptionVi = description;
        if (this.descriptionEn == null) this.descriptionEn = description;
    }
}