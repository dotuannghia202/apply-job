package com.dtn.apply_job.domain.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ResultPaginationDTO {
    private Meta meta;
    private Object result;
}
