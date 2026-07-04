package com.dtn.apply_job.domain.request.user;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqUpdateUserStatusDTO {
    @NotNull(message = "Trạng thái không được để trống!")
    private Boolean isActive;
}