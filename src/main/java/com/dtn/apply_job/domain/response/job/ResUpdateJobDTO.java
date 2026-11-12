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
public class ResUpdateJobDTO {
    UUID id;

    @JsonProperty(value = "name_vi", access = JsonProperty.Access.WRITE_ONLY)
    String nameVi;

    @JsonProperty(value = "name_en", access = JsonProperty.Access.WRITE_ONLY)
    String nameEn;

    String location;
    Double minSalary;
    Double maxSalary;
    Integer quantity;
    Set<LevelEnum> levels;

    @JsonProperty(value = "description_vi", access = JsonProperty.Access.WRITE_ONLY)
    String descriptionVi;

    @JsonProperty(value = "description_en", access = JsonProperty.Access.WRITE_ONLY)
    String descriptionEn;

    @Builder.Default
    @JsonProperty(value = "requirements_vi", access = JsonProperty.Access.WRITE_ONLY)
    List<String> requirementsVi = new ArrayList<>();

    @Builder.Default
    @JsonProperty(value = "requirements_en", access = JsonProperty.Access.WRITE_ONLY)
    List<String> requirementsEn = new ArrayList<>();

    Instant startDate;
    Instant endDate;
    Boolean active;
    Instant updatedAt;
    String updatedBy;

    @Builder.Default
    @JsonProperty(value = "benefits_vi", access = JsonProperty.Access.WRITE_ONLY)
    List<String> benefitsVi = new ArrayList<>();

    @Builder.Default
    @JsonProperty(value = "benefits_en", access = JsonProperty.Access.WRITE_ONLY)
    List<String> benefitsEn = new ArrayList<>();

    @JsonProperty(value = "working_hours_vi", access = JsonProperty.Access.WRITE_ONLY)
    String workingHoursVi;

    @JsonProperty(value = "working_hours_en", access = JsonProperty.Access.WRITE_ONLY)
    String workingHoursEn;

    String companyName;

    @JsonProperty(value = "specialization_name_vi", access = JsonProperty.Access.WRITE_ONLY)
    String specializationNameVi;

    @JsonProperty(value = "specialization_name_en", access = JsonProperty.Access.WRITE_ONLY)
    String specializationNameEn;

    @Builder.Default
    @JsonProperty(value = "skills_vi", access = JsonProperty.Access.WRITE_ONLY)
    List<String> skillsVi = new ArrayList<>();

    @Builder.Default
    @JsonProperty(value = "skills_en", access = JsonProperty.Access.WRITE_ONLY)
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

    @JsonProperty("working_hours")
    public String getWorkingHours() {
        return LanguageUtil.resolve(this.workingHoursVi, this.workingHoursEn);
    }

    public void setWorkingHours(String workingHours) {
        if (this.workingHoursVi == null) this.workingHoursVi = workingHours;
        if (this.workingHoursEn == null) this.workingHoursEn = workingHours;
    }

    @JsonProperty("requirements")
    public List<String> getRequirements() {
        return LanguageUtil.resolve(this.requirementsVi, this.requirementsEn);
    }

    public void setRequirements(List<String> requirements) {
        if (this.requirementsVi == null || this.requirementsVi.isEmpty()) this.requirementsVi = requirements;
        if (this.requirementsEn == null || this.requirementsEn.isEmpty()) this.requirementsEn = requirements;
    }

    @JsonProperty("benefits")
    public List<String> getBenefits() {
        return LanguageUtil.resolve(this.benefitsVi, this.benefitsEn);
    }

    public void setBenefits(List<String> benefits) {
        if (this.benefitsVi == null || this.benefitsVi.isEmpty()) this.benefitsVi = benefits;
        if (this.benefitsEn == null || this.benefitsEn.isEmpty()) this.benefitsEn = benefits;
    }

    @JsonProperty("skills")
    public List<String> getSkills() {
        return LanguageUtil.resolve(this.skillsVi, this.skillsEn);
    }

    public void setSkills(List<String> skills) {
        if (this.skillsVi == null || this.skillsVi.isEmpty()) this.skillsVi = skills;
        if (this.skillsEn == null || this.skillsEn.isEmpty()) this.skillsEn = skills;
    }

    @JsonProperty("specialization_name")
    public String getSpecializationName() {
        return LanguageUtil.resolve(this.specializationNameVi, this.specializationNameEn);
    }

    public void setSpecializationName(String specializationName) {
        if (this.specializationNameVi == null) this.specializationNameVi = specializationName;
        if (this.specializationNameEn == null) this.specializationNameEn = specializationName;
    }
}
