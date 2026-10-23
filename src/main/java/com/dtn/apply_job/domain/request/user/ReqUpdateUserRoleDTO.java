package com.dtn.apply_job.domain.request.user;

import com.dtn.apply_job.util.constant.enums.ERole;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReqUpdateUserRoleDTO {

    @NotEmpty(message = "Danh sách quyền không được để trống!")
    List<ERole> roles;
}