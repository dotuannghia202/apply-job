package com.dtn.apply_job.domain.request.resume;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ReqUpdateResumeDTO {

    @NotNull(message = "Candidate id is required")
    @Positive(message = "Candidate id must be positive")
    private Long candidateId;

    @NotBlank(message = "File name is required")
    private String fileName;

    @NotBlank(message = "File url is required")
    private String fileUrl;

    private String parsedText;

    @NotNull(message = "Active flag is required")
    private Boolean active;

    private List<@NotNull @Positive Long> skillIds;

    @Positive(message = "Specialization id must be positive")
    private Long specializationId;
}

