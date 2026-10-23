package com.dtn.apply_job.domain.request.company;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReqCreateCompanyDTO {

    @NotBlank(message = "Tên công ty không được để trống!")
    String name;

    @JsonProperty("description_vi")
    String descriptionVi;

    @JsonProperty("description_en")
    String descriptionEn;

    @NotBlank(message = "Địa chỉ công ty không được để trống!")
    String address;

    @NotBlank(message = "Logo công ty không được để trống!")
    String logo;

    public void setDescription(String description) {
        if (this.descriptionVi == null || this.descriptionVi.isBlank()) this.descriptionVi = description;
        if (this.descriptionEn == null || this.descriptionEn.isBlank()) this.descriptionEn = description;
    }

    public String getDescription() {
        return this.descriptionVi != null && !this.descriptionVi.isBlank() ? this.descriptionVi : this.descriptionEn;
    }

    @AssertTrue(message = "Mô tả công ty không được để trống!")
    public boolean isDescriptionValid() {
        return (descriptionVi != null && !descriptionVi.isBlank()) || (descriptionEn != null && !descriptionEn.isBlank());
    }
}
