package com.dtn.apply_job.domain.request.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqForgotPasswordDTO {
    @NotBlank(message = "Vui lòng nhập Email")
    @Email(message = "Email không hợp lệ!")
    private String email;
}