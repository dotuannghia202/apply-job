package com.dtn.apply_job.domain.request.job;

import com.dtn.apply_job.util.constant.enums.LevelEnum;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
public class ReqUpdateJobDTO {

    @NotBlank
    @Size(max = 255)
    private String name;

    @NotNull
    private String location;

    @NotNull
    @Min(0)
    private Double salary;

    private String description;
    
    @NotNull
    private LevelEnum level;

    @NotNull
    private Instant startDate;

    @NotNull
    private Instant endDate;

    @NotNull
    private Boolean active;

    @NotEmpty
    private List<@NotNull @Positive Long> skillIds;
}
