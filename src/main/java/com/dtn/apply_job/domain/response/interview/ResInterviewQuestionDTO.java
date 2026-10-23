package com.dtn.apply_job.domain.response.interview;

import com.dtn.apply_job.util.constant.enums.QuestionCategory;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ResInterviewQuestionDTO {
    UUID id;
    Integer orderIndex;
    QuestionCategory category;
    Integer timeLimitSeconds;
    Integer durationSeconds;
    Integer startTimeSeconds;

    String questionText;
    String questionTextVi;
    String questionTextEn;

    String transcribedText;
    String transcribedTextVi;
    String transcribedTextEn;

    String feedback;
    String feedbackVi;
    String feedbackEn;

    Double accuracyScore;
    Double speedFactor;
    Double finalScore;
    String evidenceQuote;
}
