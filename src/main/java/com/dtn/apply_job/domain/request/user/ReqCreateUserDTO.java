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
    @NotBlank(message = "The name cannot be left blank!")
    private String name;

    @Email(message = "Email is invalid!")
    private String email;

    @NotBlank(message = "Password cannot be left blank!")
    private String password;

    private int age;
    private GenderEnum gender;
    private String address;


    private Long companyId;

    
    private ERole roleName;
}