package com.dtn.apply_job.domain.request.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqChangePasswordDTO {

    @NotBlank(message = "Please enter your current password")
    private String oldPassword;

    @NotBlank(message = "Please enter a new password")
    @Size(min = 6, message = "New password must be at least 6 characters long")
    private String newPassword;

    @NotBlank(message = "Please confirm your new password")
    private String confirmPassword;
}