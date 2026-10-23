package com.dtn.apply_job.domain;

import com.dtn.apply_job.util.constant.enums.ApplicationStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "applications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    Job job;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false)
    Resume resume;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    ApplicationStatus status = ApplicationStatus.PENDING;

    @Column(name = "match_score")
    Double matchScore;

    @ElementCollection
    @CollectionTable(name = "application_matched_skills", joinColumns = @JoinColumn(name = "application_id"))
    @Column(name = "skill_name")
    List<String> matchedSkills;

    @ElementCollection
    @CollectionTable(name = "application_missing_skills", joinColumns = @JoinColumn(name = "application_id"))
    @Column(name = "skill_name")
    List<String> missingSkills;

    // THÔNG TIN LÊN LỊCH PHỎNG VẤN
    @Column(name = "interview_time")
    Instant interviewTime;

    @Column(name = "interview_location", columnDefinition = "TEXT")
    String interviewLocation;

    @Column(name = "interview_message", columnDefinition = "TEXT")
    String interviewMessage;

    @Column(name = "cover_letter", columnDefinition = "TEXT")
    String coverLetter;

    @Column(name = "applied_at")
    Instant appliedAt;

    @PrePersist
    protected void onCreate() {
        appliedAt = Instant.now();
    }
}