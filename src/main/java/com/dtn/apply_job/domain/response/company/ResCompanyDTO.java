package com.dtn.apply_job.domain.response.company;

import com.dtn.apply_job.util.constant.enums.CompanyStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class ResCompanyDTO {
    // Cột 1: COMPANY
    private long id;
    private String name;
    private String logo;
    private String address;
    private String description;

    // Cột 2: EMPLOYER
    private String employerName;
    private String employerEmail;

    // Cột 3 & 4: DATES & STATUS

    private Instant createdAt;

    
    private Instant updatedAt;

    private String updatedBy;

    private CompanyStatus status;
}