package com.dtn.apply_job.domain.request.interview;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReqSubmitInterviewDTO {
    List<QuestionProgress> questionProgresses;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class QuestionProgress {
        UUID questionId;
        Integer durationSeconds;
        Integer startTimeSeconds;
    }
}
