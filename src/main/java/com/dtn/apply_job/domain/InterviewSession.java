package com.dtn.apply_job.domain;

import com.dtn.apply_job.util.constant.enums.InterviewSessionStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "interview_sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InterviewSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    // Gắn 1-1 với đơn ứng tuyển
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false, unique = true)
    Application application;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    InterviewSessionStatus status = InterviewSessionStatus.NOT_STARTED;

    // Điểm tổng thể (Thang 100)
    @Column(name = "total_score")
    Double totalScore;

    // Nhận xét tổng quan của AI
    @Column(name = "ai_overall_feedback", columnDefinition = "TEXT")
    String aiOverallFeedback;

    // Link video/audio lưu trữ (nếu có)
    @Column(name = "media_record_url")
    String mediaRecordUrl;

    //Thời điểm ứng viên bat đầu phỏng vấn
    @Column(name = "started_at")
    Instant startedAt;

    //Thời điểm hoàn thành phorng vấn
    @Column(name = "completed_at")
    Instant completedAt;

    //Thời điểm lưu vào db
    @Column(name = "created_at", updatable = false)
    Instant createdAt;

    //AI phân tích xong, lưu điểm total_score = 85.5 vào DB
    @Column(name = "updated_at")
    private Instant updatedAt;

    @Builder.Default
    @OneToMany(mappedBy = "interviewSession", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC")
    @JsonIgnore
    List<InterviewQuestion> questions = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
