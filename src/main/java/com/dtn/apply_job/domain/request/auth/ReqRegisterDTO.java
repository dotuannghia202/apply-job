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
public class ReqRegisterDTO {

    @NotBlank(message = "Email không được để trống!")
    @Email(message = "Email không đúng định dạng!")
    String email;

    @NotBlank(message = "Họ tên không được để trống!")
    String name;
}
