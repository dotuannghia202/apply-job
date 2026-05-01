package com.dtn.apply_job.domain;

import com.dtn.apply_job.security.SecurityUtil;
import com.dtn.apply_job.util.constant.enums.LevelEnum;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(
        name = "jobs",
        indexes = {
                @Index(name = "idx_job_active", columnList = "active"),
                @Index(name = "idx_job_specialization", columnList = "specialization_id"),
                @Index(name = "idx_job_company", columnList = "company_id")
        }
)
@Getter
@Setter
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    private String location;

    @Column(nullable = false)
    private Double salary;

    @Column(nullable = false)
    private Integer quantity;

    @ElementCollection(targetClass = LevelEnum.class, fetch = FetchType.LAZY)
    @CollectionTable(
            name = "job_levels",
            joinColumns = @JoinColumn(name = "job_id", nullable = false),
            uniqueConstraints = {
                    @UniqueConstraint(
                            name = "uk_job_levels_job_id_level",
                            columnNames = {"job_id", "level"}
                    )
            }
    )
    @Column(name = "level", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private Set<LevelEnum> levels = new HashSet<>();

    @Column(columnDefinition = "TEXT")
    private String description;

    private Instant startDate;
    private Instant endDate;

    @Column(nullable = false)
    private Boolean active = true;

    @Column(updatable = false)
    private Instant createdAt;

    private Instant updatedAt;

    @Column(updatable = false)
    private String createdBy;

    private String updatedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "job_skill",
            joinColumns = @JoinColumn(name = "job_id"),
            inverseJoinColumns = @JoinColumn(name = "skill_id")
    )
    private List<Skill> skills;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "specialization_id")
    private Specialization specialization;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "job_benefits",
            joinColumns = @JoinColumn(name = "job_id", nullable = false)
    )
    @Column(name = "benefits", length = 255)
    private List<String> benefits;

    @Column(name = "working_hour", length = 255)
    private String workingHours;

    @PrePersist
    public void handleBeforeCreate() {
        if (this.active == null) {
            this.active = true;
        }
        this.createdBy = SecurityUtil.getCurrentUser().orElse("");
        this.createdAt = Instant.now();
    }

    @PreUpdate
    public void handleBeforeUpdate() {
        this.updatedBy = SecurityUtil.getCurrentUser().orElse("");
        this.updatedAt = Instant.now();
    }
}
