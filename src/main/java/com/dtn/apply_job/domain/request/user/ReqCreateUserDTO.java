package com.dtn.apply_job.domain.request.user;

import com.dtn.apply_job.util.constant.enums.ERole;
import com.dtn.apply_job.util.constant.enums.GenderEnum;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqCreateUserDTO {
    @NotBlank(message = "Họ tên không được để trống!")
    private String name;

    @Email(message = "Email không hợp lệ!")
    private String email;

    @NotBlank(message = "Mật khẩu không được để trống!")
    private String password;

    private int age;
    private GenderEnum gender;
    private String address;


    private Long companyId;

    
    private ERole roleName;
}