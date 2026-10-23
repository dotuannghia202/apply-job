package com.dtn.apply_job.domain.request.company;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReqUpdateCompanyDTO {

    @Size(min = 1, message = "Tên công ty không được để rỗng")
    String name;

    @Size(min = 1, message = "Địa chỉ không được để rỗng")
    String address;

    @JsonProperty("description_vi")
    String descriptionVi;

    @JsonProperty("description_en")
    String descriptionEn;

    String logo; // Link Cloudinary

    public void setDescription(String description) {
        if (this.descriptionVi == null || this.descriptionVi.isBlank()) this.descriptionVi = description;
        if (this.descriptionEn == null || this.descriptionEn.isBlank()) this.descriptionEn = description;
    }

    public String getDescription() {
        return this.descriptionVi != null && !this.descriptionVi.isBlank() ? this.descriptionVi : this.descriptionEn;
    }
}