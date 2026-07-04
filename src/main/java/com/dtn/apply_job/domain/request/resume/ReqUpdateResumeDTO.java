package com.dtn.apply_job.domain.request.resume;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ReqUpdateResumeDTO {


    @Size(min = 1, message = "Tên tệp tin không được để trống!")
    private String fileName;

    @Size(min = 1, message = "Đường dẫn tệp tin không được để trống!")
    private String fileUrl;

    
    private Boolean isActive;

    private Long specializationId;

    private List<Long> skillIds;
}