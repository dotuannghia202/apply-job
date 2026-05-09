package com.dtn.apply_job.domain.request.job;

import com.dtn.apply_job.util.constant.enums.LevelEnum;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;
import java.util.Set;

@Getter
@Setter
public class ReqUpdateJobDTO {

    @Size(max = 255, message = "Job title must be at most 255 characters!")
    private String name;

    private String location;

    @PositiveOrZero(message = "Min salary must be greater than or equal to 0!")
    private Double minSalary;

    @PositiveOrZero(message = "Max salary must be greater than or equal to 0!")
    private Double maxSalary;

    @Positive(message = "Quantity must be greater than 0!")
    private Integer quantity;

    private String description;

    private List<String> requirements;

    private Set<LevelEnum> levels;

    private Instant startDate;

    private Instant endDate;

    private Boolean isActive;

    private List<String> benefits;

    private String workingHours;

    private Long companyId;

    private Long specializationId;

    private List<@Positive(message = "Skill ID must be greater than 0!") Long> skillIds;

    @AssertTrue(message = "Max salary must be greater than or equal to min salary!")
    public boolean isSalaryRangeValid() {
        if (minSalary == null || maxSalary == null) {
            return true;
        }
        return maxSalary >= minSalary;
    }
}