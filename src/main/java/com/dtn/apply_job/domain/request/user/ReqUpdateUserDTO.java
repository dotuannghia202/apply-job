package com.dtn.apply_job.domain.request.user;

import com.dtn.apply_job.util.constant.enums.ERole;
import com.dtn.apply_job.util.constant.enums.GenderEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class ReqUpdateUserDTO {

    @NotBlank(message = "Name must not be blank!")
    private String name;
    private int age;
    private GenderEnum gender;
    private String address;
    private Boolean isActive;

    private Long companyId;

    @NotEmpty(message = "Role is required!")
    private List<ERole> roles;
}
