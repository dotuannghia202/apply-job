package com.dtn.apply_job.domain.request.job;

import com.dtn.apply_job.util.constant.enums.LevelEnum;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
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
public class ReqCreateJobDTO {

    @Size(max = 255, message = "Tiêu đề công việc tối đa 255 ký tự!")
    @JsonProperty("name_vi")
    String nameVi;

    @Size(max = 255, message = "Job title can be at most 255 characters!")
    @JsonProperty("name_en")
    String nameEn;

    @NotBlank(message = "Địa điểm làm việc không được để trống!")
    String location;

    @NotNull(message = "Lương tối thiểu không được để trống!")
    @PositiveOrZero(message = "Lương tối thiểu phải lớn hơn hoặc bằng 0!")
    Double minSalary;

    @NotNull(message = "Lương tối đa không được để trống!")
    @PositiveOrZero(message = "Lương tối đa phải lớn hơn hoặc bằng 0!")
    Double maxSalary;

    @NotNull(message = "Số lượng tuyển dụng không được để trống!")
    @Positive(message = "Số lượng tuyển dụng phải lớn hơn 0!")
    Integer quantity;

    @JsonProperty("description_vi")
    String descriptionVi;

    @JsonProperty("description_en")
    String descriptionEn;

    @JsonProperty("requirements_vi")
    List<String> requirementsVi;

    @JsonProperty("requirements_en")
    List<String> requirementsEn;

    @NotEmpty(message = "Cấp bậc không được để trống!")
    Set<@NotNull(message = "Cấp bậc không được để trống!") LevelEnum> levels;

    @NotNull(message = "Ngày bắt đầu không được để trống!")
    Instant startDate;

    @NotNull(message = "Hạn nhận hồ sơ không được để trống!")
    Instant endDate;

    Boolean active;

    @JsonProperty("benefits_vi")
    List<String> benefitsVi;

    @JsonProperty("benefits_en")
    List<String> benefitsEn;

    @JsonProperty("working_hours_vi")
    String workingHoursVi;

    @JsonProperty("working_hours_en")
    String workingHoursEn;

    @NotNull(message = "Mã công ty không được để trống!")
    UUID companyId;

    @NotNull(message = "Mã chuyên ngành không được để trống!")
    UUID specializationId;

    @NotEmpty(message = "Mã kỹ năng không được để trống!")
    List<@NotNull(message = "Mã kỹ năng không được để trống!")
            UUID> skillIds;

    // --- Backward Compatibility Handlers ---

    public void setName(String name) {
        if (this.nameVi == null || this.nameVi.isBlank()) this.nameVi = name;
        if (this.nameEn == null || this.nameEn.isBlank()) this.nameEn = name;
    }

    public String getName() {
        return this.nameVi != null && !this.nameVi.isBlank() ? this.nameVi : this.nameEn;
    }

    public void setDescription(String description) {
        if (this.descriptionVi == null || this.descriptionVi.isBlank()) this.descriptionVi = description;
        if (this.descriptionEn == null || this.descriptionEn.isBlank()) this.descriptionEn = description;
    }

    public String getDescription() {
        return this.descriptionVi != null && !this.descriptionVi.isBlank() ? this.descriptionVi : this.descriptionEn;
    }

    public void setWorkingHours(String workingHours) {
        if (this.workingHoursVi == null || this.workingHoursVi.isBlank()) this.workingHoursVi = workingHours;
        if (this.workingHoursEn == null || this.workingHoursEn.isBlank()) this.workingHoursEn = workingHours;
    }

    public String getWorkingHours() {
        return this.workingHoursVi != null && !this.workingHoursVi.isBlank() ? this.workingHoursVi : this.workingHoursEn;
    }

    public void setRequirements(List<String> requirements) {
        if (this.requirementsVi == null || this.requirementsVi.isEmpty()) this.requirementsVi = requirements;
        if (this.requirementsEn == null || this.requirementsEn.isEmpty()) this.requirementsEn = requirements;
    }

    public List<String> getRequirements() {
        return this.requirementsVi != null && !this.requirementsVi.isEmpty() ? this.requirementsVi : this.requirementsEn;
    }

    public void setBenefits(List<String> benefits) {
        if (this.benefitsVi == null || this.benefitsVi.isEmpty()) this.benefitsVi = benefits;
        if (this.benefitsEn == null || this.benefitsEn.isEmpty()) this.benefitsEn = benefits;
    }

    public List<String> getBenefits() {
        return this.benefitsVi != null && !this.benefitsVi.isEmpty() ? this.benefitsVi : this.benefitsEn;
    }

    @AssertTrue(message = "Tiêu đề công việc không được để trống!")
    public boolean isTitleValid() {
        return (nameVi != null && !nameVi.isBlank()) || (nameEn != null && !nameEn.isBlank());
    }

    @AssertTrue(message = "Lương tối đa phải lớn hơn hoặc bằng lương tối thiểu!")
    public boolean isSalaryRangeValid() {
        if (minSalary == null || maxSalary == null) {
            return true;
        }
        return maxSalary >= minSalary;
    }
}