package com.dtn.apply_job.domain.response.job;

import com.dtn.apply_job.util.constant.enums.LevelEnum;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;
import java.util.Set;

@Getter
@Setter
public class ResUpdateJobDTO {
    private Long id;
    private String name;
    private String location;
    private Double minSalary;
    private Double maxSalary;
    private Integer quantity;
    private Set<LevelEnum> levels;
    private String description;
    private List<String> requirements;
    private Instant startDate;
    private Instant endDate;
    private Boolean active;
    private Instant updatedAt;
    private String updatedBy;
    private List<String> benefits;
    private String workingHours;
    private String companyName;
    private String specializationName;
    private List<String> skills;
}
