package com.dtn.apply_job.domain;

import com.dtn.apply_job.security.SecurityUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "specializations")
@Getter
@Setter
public class Specialization {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotBlank(message = "\n" +
            "The field name cannot be left blank!")
    private String name;

    // Trực thuộc Ngành nghề nào?
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "industry_id")
    private Industry industry;

    // Các tin tuyển dụng thuộc mảng này
    @OneToMany(mappedBy = "specialization", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Job> jobs;

    // Các hồ sơ CV thuộc mảng này
    @OneToMany(mappedBy = "specialization", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Resume> resumes;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss a", timezone = "GMT+7")
    private Instant createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss a", timezone = "GMT+7")
    private Instant updatedAt;

    private String createdBy;
    private String updatedBy;

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