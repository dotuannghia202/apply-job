package com.dtn.apply_job.domain.request.user;

import com.dtn.apply_job.util.constant.enums.ERole;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ReqUpdateUserRoleDTO {

    // Yêu cầu phải gửi lên ít nhất 1 quyền (Không được để trống rỗng mảng)
    @NotEmpty(message = "The list of permissions cannot be left blank!")
    private List<ERole> roles;
}