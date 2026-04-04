package com.dtn.apply_job.domain;

import com.dtn.apply_job.util.constant.enums.ApplicationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "applications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Nộp vào Công việc nào?
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    // Sử dụng CV nào để nộp?
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false)
    private Resume resume;

    // Trạng thái đơn (Dùng Enum)
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ApplicationStatus status = ApplicationStatus.PENDING;

    // VÙNG DỮ LIỆU DÀNH CHO AI: Lưu điểm % phù hợp do AI chấm
    @Column(name = "match_score")
    private Double matchScore;

    // Lời nhắn gửi kèm của ứng viên (Cover Letter)
    @Column(name = "cover_letter", columnDefinition = "TEXT")
    private String coverLetter;

    @Column(name = "applied_at")
    private LocalDateTime appliedAt;

    // Hàm tự động gán ngày nộp lúc Insert dữ liệu
    @PrePersist
    protected void onCreate() {
        appliedAt = LocalDateTime.now();
    }
}
