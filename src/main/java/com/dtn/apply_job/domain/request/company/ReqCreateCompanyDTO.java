package com.dtn.apply_job.domain.request.company;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ReqCreateCompanyDTO {

    @NotBlank(message = "Field name is required!")
    private String name;

    @NotBlank(message = "Field description is required!")
    private String description;

    @NotBlank(message = "Field address is required!")
    private String address;

    @NotBlank(message = "Field logo is required!")
    private String logo;
}
