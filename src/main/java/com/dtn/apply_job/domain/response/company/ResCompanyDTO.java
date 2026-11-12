package com.dtn.apply_job.domain.response.company;

import com.dtn.apply_job.util.LanguageUtil;
import com.dtn.apply_job.util.constant.enums.CompanyStatus;
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
public class ResCompanyDTO {
    // Cột 1: COMPANY
    UUID id;
    String name;
    String logo;
    String address;

    @JsonProperty(value = "description_vi", access = JsonProperty.Access.WRITE_ONLY)
    String descriptionVi;

    @JsonProperty(value = "description_en", access = JsonProperty.Access.WRITE_ONLY)
    String descriptionEn;

    // Cột 2: EMPLOYER
    String employerName;
    String employerEmail;

    // Cột 3 & 4: DATES & STATUS
    Instant createdAt;
    Instant updatedAt;
    String updatedBy;
    CompanyStatus status;

    @JsonProperty("description")
    public String getDescription() {
        return LanguageUtil.resolve(this.descriptionVi, this.descriptionEn);
    }

    public void setDescription(String description) {
        if (this.descriptionVi == null) this.descriptionVi = description;
        if (this.descriptionEn == null) this.descriptionEn = description;
    }
}