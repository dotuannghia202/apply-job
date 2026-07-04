package com.dtn.apply_job.domain.request.user;

import com.dtn.apply_job.util.constant.enums.GenderEnum;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ReqUpdateUserDTO {

    @NotBlank(message = "Họ tên không được để trống!")
    private String name;
    private String avatarUrl;
    private int age;
    private GenderEnum gender;
    private String address;
}
