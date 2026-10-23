package com.dtn.apply_job.domain.request.user;

import com.dtn.apply_job.util.constant.enums.ERole;
import com.dtn.apply_job.util.constant.enums.GenderEnum;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReqCreateUserDTO {
    @NotBlank(message = "Họ tên không được để trống!")
    String name;

    @Email(message = "Email không hợp lệ!")
    String email;

    @NotBlank(message = "Mật khẩu không được để trống!")
    String password;

    int age;
    GenderEnum gender;
    String address;

    UUID companyId;

    ERole roleName;
}