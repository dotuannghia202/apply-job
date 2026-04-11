package com.dtn.apply_job.domain.response.resume;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class ResResumeDTO {

    private Long id;
    private Long candidateId;
    private String fileName;
    private String fileUrl;
    private String parsedText;
    private boolean active;
    private List<Long> skillIds;
    private Long specializationId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

