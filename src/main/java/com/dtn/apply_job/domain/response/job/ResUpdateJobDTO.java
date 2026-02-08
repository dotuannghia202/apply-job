package com.dtn.apply_job.domain.response.job;

import com.dtn.apply_job.util.constant.enums.LevelEnum;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Column;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Setter
@Getter
public class ResUpdateJobDTO {
    private long id;
    private String name;
    private String location;
    private Double salary;
    private int quantity;
    private LevelEnum level;
    @Column(columnDefinition = "MEDIUMTEXT")
    private String description;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss a", timezone = "GMT+7")
    private Instant startDate;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss a", timezone = "GMT+7")
    private Instant endDate;
    private boolean active;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss a", timezone = "GMT+7")
    private Instant updatedAt;
    private String updatedBy;
    List<String> skills;
}
