package com.dtn.apply_job.domain.response.file;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class ResDownloadFileDTO {
    private String downloadUrl;
    private String fileName;
}
