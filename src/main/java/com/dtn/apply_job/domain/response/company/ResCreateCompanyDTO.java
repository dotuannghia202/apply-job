package com.dtn.apply_job.domain.response.company;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Setter
@Getter
public class ResCreateCompanyDTO {
    private long id;
    private String name;
    private String description;
    private String address;
    private String logo;


    private Instant createdAt;

    private String createdBy;
}