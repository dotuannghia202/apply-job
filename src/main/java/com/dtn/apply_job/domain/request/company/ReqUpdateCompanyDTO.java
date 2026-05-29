package com.dtn.apply_job.domain.request.company;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqUpdateCompanyDTO {

    @Size(min = 1, message = "Tên công ty không được để rỗng")
    private String name;

    @Size(min = 1, message = "Địa chỉ không được để rỗng")
    private String address;

    private String description;

    private String logo; // Link Cloudinary
}