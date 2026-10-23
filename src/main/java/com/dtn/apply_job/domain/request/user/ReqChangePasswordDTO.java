package com.dtn.apply_job.domain.request.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReqChangePasswordDTO {

    @NotBlank(message = "Vui lòng nhập mật khẩu hiện tại")
    String oldPassword;

    @NotBlank(message = "Vui lòng nhập mật khẩu mới")
    @Size(min = 6, message = "Mật khẩu mới phải dài ít nhất 6 ký tự")
    String newPassword;

    @NotBlank(message = "Vui lòng xác nhận mật khẩu mới")
    String confirmPassword;
}