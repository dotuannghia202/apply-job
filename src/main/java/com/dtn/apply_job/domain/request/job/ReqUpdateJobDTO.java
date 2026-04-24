package com.dtn.apply_job.domain.request.job;

import com.dtn.apply_job.util.constant.enums.LevelEnum;
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

    @PositiveOrZero(message = "Salary must be greater than or equal to 0!")
    private Double salary;

    @Positive(message = "Quantity must be greater than 0!")
    private Integer quantity;

    private String description;

    private Set<LevelEnum> levels;

    private Instant startDate;

    private Instant endDate;

    private Boolean isActive;

    private Long companyId;

    private Long specializationId;

    private List<@Positive(message = "Skill ID must be greater than 0!") Long> skillIds;
}