package com.dtn.apply_job.domain.request.application;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqUpdateAppByCandidateDTO {
    // Dùng Long và String (Object) để cho phép truyền null nếu chỉ muốn sửa 1 trong 2
    private Long resumeId;
    private String coverLetter;
}
