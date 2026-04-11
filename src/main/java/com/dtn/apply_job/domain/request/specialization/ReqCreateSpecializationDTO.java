package com.dtn.apply_job.domain.request.specialization;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class ReqCreateSpecializationDTO {
    private String name;
    private long industryId;
}
