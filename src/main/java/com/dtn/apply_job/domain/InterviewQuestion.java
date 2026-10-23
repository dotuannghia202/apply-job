package com.dtn.apply_job.domain;

import com.dtn.apply_job.util.LanguageUtil;
import com.dtn.apply_job.util.constant.enums.QuestionCategory;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Entity
@Table(name = "interview_questions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InterviewQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    InterviewSession interviewSession;

    @Column(name = "order_index", nullable = false)
    Integer orderIndex;

    // Câu hỏi song ngữ
    @Column(name = "question_text_vi", columnDefinition = "TEXT")
    @JsonProperty("question_text_vi")
    String questionTextVi;

    @Column(name = "question_text_en", columnDefinition = "TEXT")
    @JsonProperty("question_text_en")
    String questionTextEn;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    QuestionCategory category;

    @Column(name = "time_limit_seconds", nullable = false)
    Integer timeLimitSeconds;

    @Column(name = "rubric", columnDefinition = "TEXT")
    String rubric;

    // Dữ liệu thời gian làm bài của ứng viên
    @Column(name = "duration_seconds")
    Integer durationSeconds;

    @Column(name = "start_time_seconds")
    Integer startTimeSeconds;

    // Lời thoại bóc băng song ngữ
    @Column(name = "transcribed_text_vi", columnDefinition = "TEXT")
    @JsonProperty("transcribed_text_vi")
    String transcribedTextVi;

    @Column(name = "transcribed_text_en", columnDefinition = "TEXT")
    @JsonProperty("transcribed_text_en")
    String transcribedTextEn;

    // Chấm điểm & Bằng chứng chống ảo giác
    @Column(name = "accuracy_score")
    Double accuracyScore;

    @Column(name = "speed_factor")
    Double speedFactor;

    @Column(name = "final_score")
    Double finalScore;

    @Column(name = "evidence_quote", columnDefinition = "TEXT")
    String evidenceQuote;

    // Nhận xét song ngữ
    @Column(name = "feedback_vi", columnDefinition = "TEXT")
    @JsonProperty("feedback_vi")
    String feedbackVi;

    @Column(name = "feedback_en", columnDefinition = "TEXT")
    @JsonProperty("feedback_en")
    String feedbackEn;

    @JsonProperty("question_text")
    public String getQuestionText() {
        return LanguageUtil.resolve(this.questionTextVi, this.questionTextEn);
    }

    @JsonProperty("transcribed_text")
    public String getTranscribedText() {
        return LanguageUtil.resolve(this.transcribedTextVi, this.transcribedTextEn);
    }

    @JsonProperty("feedback")
    public String getFeedback() {
        return LanguageUtil.resolve(this.feedbackVi, this.feedbackEn);
    }

    @PrePersist
    public void handleBeforeCreate() {
        if (this.questionTextVi == null && this.questionTextEn != null) this.questionTextVi = this.questionTextEn;
        if (this.questionTextEn == null && this.questionTextVi != null) this.questionTextEn = this.questionTextVi;
    }
}