package com.dtn.apply_job.domain.response.job;

import com.dtn.apply_job.util.LanguageUtil;
import com.dtn.apply_job.util.constant.enums.LevelEnum;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ResCreateJobDTO {
    UUID id;

    @JsonProperty("name_vi")
    String nameVi;

    @JsonProperty("name_en")
    String nameEn;

    String location;
    Double salary;
    int quantity;
    Set<LevelEnum> levels;

    @JsonProperty("description_vi")
    String descriptionVi;

    @JsonProperty("description_en")
    String descriptionEn;

    Instant startDate;
    Instant endDate;
    boolean active;
    Instant createdAt;
    String createdBy;

    @Builder.Default
    @JsonProperty("skills_vi")
    List<String> skillsVi = new ArrayList<>();

    @Builder.Default
    @JsonProperty("skills_en")
    List<String> skillsEn = new ArrayList<>();

    @JsonProperty("name")
    public String getName() {
        return LanguageUtil.resolve(this.nameVi, this.nameEn);
    }

    public void setName(String name) {
        if (this.nameVi == null) this.nameVi = name;
        if (this.nameEn == null) this.nameEn = name;
    }

    @JsonProperty("description")
    public String getDescription() {
        return LanguageUtil.resolve(this.descriptionVi, this.descriptionEn);
    }

    public void setDescription(String description) {
        if (this.descriptionVi == null) this.descriptionVi = description;
        if (this.descriptionEn == null) this.descriptionEn = description;
    }

    @JsonProperty("skills")
    public List<String> getSkills() {
        return LanguageUtil.resolve(this.skillsVi, this.skillsEn);
    }

    public void setSkills(List<String> skills) {
        if (this.skillsVi == null || this.skillsVi.isEmpty()) this.skillsVi = skills;
        if (this.skillsEn == null || this.skillsEn.isEmpty()) this.skillsEn = skills;
    }
}
