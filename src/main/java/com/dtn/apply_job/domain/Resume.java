package com.dtn.apply_job.domain;

import com.dtn.apply_job.security.SecurityUtil;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "resumes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Resume {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    // Quan hệ N-1: Nhiều CV thuộc về 1 Ứng viên (Bảng User)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id", nullable = false)
    User candidate;

    @Column(name = "file_name", nullable = false)
    String fileName;

    @Column(name = "file_url", nullable = false)
    String fileUrl;

    // VÙNG DỮ LIỆU DÀNH CHO AI (Lưu text đọc ra từ PDF)
    // Lưu ý: Dùng TEXT cho PostgreSQL
    @Column(name = "parsed_text", columnDefinition = "TEXT")
    String parsedText;

    @Column(name = "is_active")
    boolean active;

    @Builder.Default
    @Column(name = "is_default")
    boolean isDefault = false;

    // Tùy chọn: Kết nối CV với các Kỹ năng (Bảng Skill)
    @ManyToMany
    @JoinTable(
            name = "resume_skill",
            joinColumns = @JoinColumn(name = "resume_id"),
            inverseJoinColumns = @JoinColumn(name = "skill_id")
    )
    List<Skill> skills;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "specialization_id")
    Specialization specialization;

    @Column(name = "created_at", updatable = false)
    Instant createdAt;

    @Column(name = "updated_at")
    Instant updatedAt;

    @Column(name = "created_by", updatable = false)
    String createdBy;

    @Column(name = "updated_by")
    String updatedBy;

    @PrePersist
    public void handleBeforeCreate() {
        this.createdBy = SecurityUtil.getCurrentUser().orElse("system");
        this.createdAt = Instant.now();
    }

    @PreUpdate
    public void handleBeforeUpdate() {
        this.updatedBy = SecurityUtil.getCurrentUser().orElse("system");
        this.updatedAt = Instant.now();
    }
}