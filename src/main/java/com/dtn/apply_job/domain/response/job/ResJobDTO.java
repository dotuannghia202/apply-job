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
public class ResJobDTO {
    UUID id;

    @JsonProperty("name_vi")
    String nameVi;

    @JsonProperty("name_en")
    String nameEn;

    String location;
    Double minSalary;
    Double maxSalary;
    Integer quantity;
    Set<LevelEnum> levels;

    @JsonProperty("description_vi")
    String descriptionVi;

    @JsonProperty("description_en")
    String descriptionEn;

    @Builder.Default
    @JsonProperty("requirements_vi")
    List<String> requirementsVi = new ArrayList<>();

    @Builder.Default
    @JsonProperty("requirements_en")
    List<String> requirementsEn = new ArrayList<>();

    Instant startDate;
    Instant endDate;
    Boolean active;
    Instant createdAt;
    String createdBy;

    @Builder.Default
    @JsonProperty("benefits_vi")
    List<String> benefitsVi = new ArrayList<>();

    @Builder.Default
    @JsonProperty("benefits_en")
    List<String> benefitsEn = new ArrayList<>();

    @JsonProperty("working_hours_vi")
    String workingHoursVi;

    @JsonProperty("working_hours_en")
    String workingHoursEn;

    @Builder.Default
    @JsonProperty("skills_vi")
    List<String> skillsVi = new ArrayList<>();

    @Builder.Default
    @JsonProperty("skills_en")
    List<String> skillsEn = new ArrayList<>();

    CompanyInfo company;
    SpecializationInfo specialization;
    Boolean isSaved;
    Boolean isApplied;
    long applicantCount;

    // --- Dynamic Locale Resolvers for Existing Frontend Compatibility ---

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

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class CompanyInfo {
        UUID id;
        String name;
        String logo;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class SpecializationInfo {
        UUID id;

        @JsonProperty("name_vi")
        String nameVi;

        @JsonProperty("name_en")
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
