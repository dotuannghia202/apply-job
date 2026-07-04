package com.dtn.apply_job.domain.request.specialization;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReqCreateSpecializationDTO {
    @NotBlank(message = "Tên chuyên ngành không được để trống")
    private String name;

    @NotNull(message = "Mã ngành nghề không được để trống")
    @Positive(message = "Mã ngành nghề phải lớn hơn 0")
    private Long industryId;
}
