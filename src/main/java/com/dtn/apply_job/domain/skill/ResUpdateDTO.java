package com.dtn.apply_job.domain.skill;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Setter
@Getter
public class ResUpdateDTO {
    private String name;
    private Instant updatedAt;
    private String updatedBy;
}
