package com.dtn.apply_job.domain.request.user;

import com.dtn.apply_job.util.constant.enums.GenderEnum;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReqUpdateUserDTO {

    @NotBlank(message = "Họ tên không được để trống!")
    String name;
    String avatarUrl;
    int age;
    GenderEnum gender;
    String address;
}
