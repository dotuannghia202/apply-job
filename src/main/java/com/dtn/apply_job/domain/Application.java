package com.dtn.apply_job.domain;

import com.dtn.apply_job.util.constant.enums.ApplicationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.List;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false)
    private Resume resume;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ApplicationStatus status = ApplicationStatus.PENDING;

    @Column(name = "match_score")
    private Double matchScore;

    @ElementCollection
    @CollectionTable(name = "application_matched_skills", joinColumns = @JoinColumn(name = "application_id"))
    @Column(name = "skill_name")
    private List<String> matchedSkills;

    @ElementCollection
    @CollectionTable(name = "application_missing_skills", joinColumns = @JoinColumn(name = "application_id"))
    @Column(name = "skill_name")
    private List<String> missingSkills;


    // THÔNG TIN LÊN LỊCH PHỎNG VẤN
    @Column(name = "interview_time")
    private Instant interviewTime;

    @Column(name = "interview_location", columnDefinition = "TEXT")
    private String interviewLocation;

    @Column(name = "interview_message", columnDefinition = "TEXT")
    private String interviewMessage;

    @Column(name = "cover_letter", columnDefinition = "TEXT")
    private String coverLetter;

    @Column(name = "applied_at")
    private Instant appliedAt;

    @PrePersist
    protected void onCreate() {
        appliedAt = Instant.now();
    }
}