package com.dtn.apply_job.domain.request.user;

import com.dtn.apply_job.util.constant.enums.ERole;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ReqUpdateUserRoleDTO {

    @NotEmpty(message = "Danh sách quyền không được để trống!")
    private List<ERole> roles;
}