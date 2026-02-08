package com.dtn.apply_job.domain;

import com.dtn.apply_job.util.SecurityUtil;
import com.dtn.apply_job.util.constant.enums.LevelEnum;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Entity
@Table(
        name = "jobs",
        indexes = {
                @Index(name = "idx_job_active", columnList = "active"),
                @Index(name = "idx_job_level", columnList = "level"),
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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LevelEnum level;

    @Column(columnDefinition = "MEDIUMTEXT")
    private String description;

    private Instant startDate;
    private Instant endDate;

    @Column(nullable = false)
    private Boolean active;

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

    @PrePersist
    public void handleBeforeCreate() {
        this.createdBy = SecurityUtil.getCurrentUser().orElse("");
        this.createdAt = Instant.now();
    }

    @PreUpdate
    public void handleBeforeUpdate() {
        this.updatedBy = SecurityUtil.getCurrentUser().orElse("");
        this.updatedAt = Instant.now();
    }
}

