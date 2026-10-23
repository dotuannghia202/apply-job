package com.dtn.apply_job.domain.request.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReqForgotPasswordDTO {
    @NotBlank(message = "Vui lòng nhập Email")
    @Email(message = "Email không hợp lệ!")
    String email;
}