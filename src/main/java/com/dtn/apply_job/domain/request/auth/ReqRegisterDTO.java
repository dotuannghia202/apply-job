package com.dtn.apply_job.domain.request.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqRegisterDTO {

    @NotBlank(message = "Email is required!")
    @Email(message = "Email is not in the correct format!")
    private String email;

    @NotBlank(message = "The full name must not be left blank!")
    private String name;
}
