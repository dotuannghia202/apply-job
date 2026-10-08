package com.dtn.apply_job.domain;

import com.dtn.apply_job.util.constant.enums.QuestionCategory;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    InterviewSession interviewSession;

    //Số thứ tự câu hỏi dùng để frontend hiển thị lần lượt
    @Column(name = "order_index", nullable = false)
    Integer orderIndex;

    //Nội dung câu hỏi
    @Column(name = "question_text", columnDefinition = "TEXT", nullable = false)
    String questionText;

    //Phân loại câu hỏi
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    QuestionCategory category;

    //Thời gian tối đa để trả loi câu hỏi này
    @Column(name = "time_limit_seconds", nullable = false)
    Integer timeLimitSeconds; // Mặc định 90s

    // Tiêu chí chấm điểm ngầm mà AI sinh ra
    @Column(name = "rubric", columnDefinition = "TEXT")
    String rubric;

    // --- KẾT QUẢ TRẢ LỜI CỦA ỨNG VIÊN ---

    //Thời gian ứng viên trả lời câu hỏi,đùng để tính điểm tốc độ
    @Column(name = "duration_seconds")
    Integer durationSeconds;

    //Link file ghi âm của giọng nói của ứng viên
    @Column(name = "audio_url")
    String audioUrl;

    //Văn bản được bóc tách từ file ghi âm phỏng vấn
    @Column(name = "transcribed_text", columnDefinition = "TEXT")
    String transcribedText;

    //KẾT QUẢ CHẤM ĐIỂM CỦA AI (Cập nhật sau khi nộp bài)
    //AI đánh giá câu trả lời của ứng viên khớp được bao nhiêu % các ý yêu cầu trong rubric
    @Column(name = "accuracy_score")
    Double accuracyScore;

    //Hệ số tốc độ (0.0 đến 1.0),Nếu trả lời sai (accuracyScore < 50) thì hệ số này tự động bị set về 0.
    @Column(name = "speed_factor")
    Double speedFactor;

    //Điểm tổng kết cuối cùng của câu hỏi này, kết hợp giữa độ chính xác và tốc độ:
    //finalScore=accuracyScore×(0.7+0.3×speedFactor)
    @Column(name = "final_score")
    Double finalScore;

    //Lời nhận xét chi tiết của AI cho riêng câu này
    //(VD: "Ứng viên hiểu đúng bản chất Redis nhưng quên nhắc đến giải pháp phòng chống Cache Avalanche khi hệ thống sập").
    @Column(name = "feedback", columnDefinition = "TEXT")
    String feedback;
}