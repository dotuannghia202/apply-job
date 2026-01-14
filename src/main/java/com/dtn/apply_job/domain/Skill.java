package com.dtn.apply_job.domain;

import com.dtn.apply_job.util.SecurityUtil;
import com.dtn.apply_job.util.constant.enums.LevelEnum;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Setter
@Getter
@Entity
@Table(name = "skills")
public class Skill {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(unique = true)
    @NotNull(message = "Level name is not null!")
    @Enumerated(EnumType.STRING)
    private LevelEnum name;


    private Instant createdAt;
    private Instant updatedAt;

    private String createdBy;
    private String updatedBy;


    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "skills")
    @JsonIgnore
    private List<Job> jobs;

    @PrePersist
    public void handleBeforeCreate() {
        this.createdBy = SecurityUtil.getCurrentUser().isPresent() == true ? SecurityUtil.getCurrentUser().get() : "";
        this.createdAt = Instant.now();
    }

    @PreUpdate
    public void handleBeforeUpdate() {
        this.updatedBy = SecurityUtil.getCurrentUser().isPresent() == true ? SecurityUtil.getCurrentUser().get() : "";
        this.updatedAt = Instant.now();
    }

}
