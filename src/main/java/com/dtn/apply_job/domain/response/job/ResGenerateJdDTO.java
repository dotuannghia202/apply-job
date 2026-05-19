package com.dtn.apply_job.domain.response.job;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ResGenerateJdDTO {
    private String description;
    private List<String> requirements;
    private List<String> benefits;
}