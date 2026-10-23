package com.dtn.apply_job.domain.response.interview;

import com.dtn.apply_job.util.constant.enums.InterviewSessionStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ResInterviewSessionDTO {
    UUID id;
    InterviewSessionStatus status;
    Double totalScore;
    String aiOverallFeedback;
    String aiOverallFeedbackVi;
    String aiOverallFeedbackEn;
    String mediaRecordUrl;
    Instant startedAt;
    Instant completedAt;
    List<ResInterviewQuestionDTO> questions;
}
