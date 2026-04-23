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
    @NotBlank(message = "Name must not be blank")
    private String name;

    @NotNull(message = "Industry id is required")
    @Positive(message = "Industry id must be greater than 0")
    private Long industryId;
}
