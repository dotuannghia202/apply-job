package com.dtn.apply_job.domain.response.job;

import com.dtn.apply_job.util.constant.enums.LevelEnum;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;
import java.util.Set;

@Setter
@Getter
public class ResUpdateJobDTO {
    private long id;
    private String name;
    private String location;
    private Double salary;
    private int quantity;
    private Set<LevelEnum> levels;

    private String description;

    private Instant startDate;

    private Instant endDate;
    private boolean active;

    private Instant updatedAt;
    private String updatedBy;

    private String companyName;
    private String specializationName;
    List<String> skills;

    private List<String> benefits;

    private String workingHours;
}
