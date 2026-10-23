package com.dtn.apply_job.domain.request.specialization;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReqCreateSpecializationDTO {

    @JsonProperty("name_vi")
    String nameVi;

    @JsonProperty("name_en")
    String nameEn;

    @NotNull(message = "Mã ngành nghề không được để trống")
    UUID industryId;

    public void setName(String name) {
        if (this.nameVi == null || this.nameVi.isBlank()) this.nameVi = name;
        if (this.nameEn == null || this.nameEn.isBlank()) this.nameEn = name;
    }

    public String getName() {
        return this.nameVi != null && !this.nameVi.isBlank() ? this.nameVi : this.nameEn;
    }

    @AssertTrue(message = "Tên chuyên ngành không được để trống")
    public boolean isNameValid() {
        return (nameVi != null && !nameVi.isBlank()) || (nameEn != null && !nameEn.isBlank());
    }
}
