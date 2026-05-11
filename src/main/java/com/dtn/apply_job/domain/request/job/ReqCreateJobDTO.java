package com.dtn.apply_job.domain.request.job;

import com.dtn.apply_job.util.constant.enums.LevelEnum;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;
import java.util.Set;

@Getter
@Setter
public class ReqCreateJobDTO {

    @NotBlank(message = "Job title must not be left blank!")
    @Size(max = 255, message = "Job title must be at most 255 characters!")
    private String name;

    @NotBlank(message = "Location must not be left blank!")
    private String location;

    @NotNull(message = "Min salary must not be null!")
    @PositiveOrZero(message = "Min salary must be greater than or equal to 0!")
    private Double minSalary;

    @NotNull(message = "Max salary must not be null!")
    @PositiveOrZero(message = "Max salary must be greater than or equal to 0!")
    private Double maxSalary;

    @NotNull(message = "Quantity must not be null!")
    @Positive(message = "Quantity must be greater than 0!")
    private Integer quantity;

    @NotBlank(message = "Description must not be left blank!")
    private String description;

    @NotEmpty(message = "Requirements must not be empty!")
    private List<@NotBlank(message = "Requirement must not be blank!") String> requirements;

    @NotEmpty(message = "Levels must not be empty!")
    private Set<@NotNull(message = "Level must not be null!") LevelEnum> levels;

    @NotNull(message = "Start date must not be null!")
    private Instant startDate;

    @NotNull(message = "End date must not be null!")
    private Instant endDate;

    // Không bắt buộc gửi lên.
    // Nếu null thì BE tự set mặc định = true khi create.
    private Boolean active;

    private List<String> benefits;

    private String workingHours;

    @NotNull(message = "Company ID must not be null!")
    private Long companyId;

    @NotNull(message = "Specialization ID must not be null!")
    private Long specializationId;

    @NotEmpty(message = "Skill IDs must not be empty!")
    private List<@NotNull(message = "Skill ID must not be null!")
    @Positive(message = "Skill ID must be greater than 0!")
            Long> skillIds;

    @AssertTrue(message = "Max salary must be greater than or equal to min salary!")
    public boolean isSalaryRangeValid() {
        if (minSalary == null || maxSalary == null) {
            return true;
        }
        return maxSalary >= minSalary;
    }
}