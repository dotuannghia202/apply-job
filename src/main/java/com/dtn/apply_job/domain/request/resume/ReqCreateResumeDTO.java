package com.dtn.apply_job.domain.request.resume;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ReqCreateResumeDTO {

    @NotBlank(message = "Tên tệp tin không được để trống")
    private String fileName;

    @NotBlank(message = "Đường dẫn tệp tin không được để trống")
    private String fileUrl;


    private Long specializationId;


    private List<Long> skillIds;

    
}