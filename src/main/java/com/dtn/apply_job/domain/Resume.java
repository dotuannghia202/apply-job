package com.dtn.apply_job.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "resumes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Resume {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Quan hệ N-1: Nhiều CV thuộc về 1 Ứng viên (Bảng User)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id", nullable = false)
    private User candidate;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "file_url", nullable = false)
    private String fileUrl;

    // VÙNG DỮ LIỆU DÀNH CHO AI (Lưu text đọc ra từ PDF)
    // Lưu ý: Dùng TEXT cho PostgreSQL
    @Column(name = "parsed_text", columnDefinition = "TEXT")
    private String parsedText;

    @Column(name = "is_active")
    private boolean active;

    // Tùy chọn: Kết nối CV với các Kỹ năng (Bảng Skill)
    @ManyToMany
    @JoinTable(
            name = "resume_skill",
            joinColumns = @JoinColumn(name = "resume_id"),
            inverseJoinColumns = @JoinColumn(name = "skill_id")
    )
    private List<Skill> skills;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "specialization_id")
    private Specialization specialization;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}