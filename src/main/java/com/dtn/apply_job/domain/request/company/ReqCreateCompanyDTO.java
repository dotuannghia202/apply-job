package com.dtn.apply_job.domain.request.company;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ReqCreateCompanyDTO {

    @NotBlank(message = "Tên công ty không được để trống!")
    private String name;

    @NotBlank(message = "Mô tả công ty không được để trống!")
    private String description;

    @NotBlank(message = "Địa chỉ công ty không được để trống!")
    private String address;

    @NotBlank(message = "Logo công ty không được để trống!")
    private String logo;
}
