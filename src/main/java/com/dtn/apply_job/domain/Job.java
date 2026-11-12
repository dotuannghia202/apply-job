package com.dtn.apply_job.domain;

import com.dtn.apply_job.security.SecurityUtil;
import com.dtn.apply_job.util.LanguageUtil;
import com.dtn.apply_job.util.constant.enums.LevelEnum;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

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
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @Column(name = "name_vi", length = 255)
    @JsonProperty(value = "name_vi", access = JsonProperty.Access.WRITE_ONLY)
    String nameVi;

    @Column(name = "name_en", length = 255)
    @JsonProperty(value = "name_en", access = JsonProperty.Access.WRITE_ONLY)
    String nameEn;

    String location;

    @Column(name = "min_salary")
    Double minSalary;

    @Column(name = "max_salary")
    Double maxSalary;

    @Column(nullable = false)
    Integer quantity;

    @Builder.Default
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
    Set<LevelEnum> levels = new HashSet<>();

    @Column(name = "description_vi", columnDefinition = "TEXT")
    @JsonProperty(value = "description_vi", access = JsonProperty.Access.WRITE_ONLY)
    String descriptionVi;

    @Column(name = "description_en", columnDefinition = "TEXT")
    @JsonProperty(value = "description_en", access = JsonProperty.Access.WRITE_ONLY)
    String descriptionEn;

    @Builder.Default
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "job_requirements_vi",
            joinColumns = @JoinColumn(name = "job_id", nullable = false)
    )
    @Column(name = "requirement", length = 500)
    @JsonProperty(value = "requirements_vi", access = JsonProperty.Access.WRITE_ONLY)
    List<String> requirementsVi = new ArrayList<>();

    @Builder.Default
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "job_requirements_en",
            joinColumns = @JoinColumn(name = "job_id", nullable = false)
    )
    @Column(name = "requirement", length = 500)
    @JsonProperty(value = "requirements_en", access = JsonProperty.Access.WRITE_ONLY)
    List<String> requirementsEn = new ArrayList<>();

    Instant startDate;
    Instant endDate;

    @Builder.Default
    @Column(nullable = false)
    Boolean active = true;

    @Column(updatable = false)
    Instant createdAt;

    Instant updatedAt;

    @Column(updatable = false)
    String createdBy;

    String updatedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    Company company;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "job_skill",
            joinColumns = @JoinColumn(name = "job_id"),
            inverseJoinColumns = @JoinColumn(name = "skill_id")
    )
    List<Skill> skills;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "specialization_id")
    Specialization specialization;

    @Builder.Default
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "job_benefits_vi",
            joinColumns = @JoinColumn(name = "job_id", nullable = false)
    )
    @Column(name = "benefit", length = 255)
    @JsonProperty(value = "benefits_vi", access = JsonProperty.Access.WRITE_ONLY)
    List<String> benefitsVi = new ArrayList<>();

    @Builder.Default
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "job_benefits_en",
            joinColumns = @JoinColumn(name = "job_id", nullable = false)
    )
    @Column(name = "benefit", length = 255)
    @JsonProperty(value = "benefits_en", access = JsonProperty.Access.WRITE_ONLY)
    List<String> benefitsEn = new ArrayList<>();

    @Column(name = "working_hours_vi", length = 255)
    @JsonProperty(value = "working_hours_vi", access = JsonProperty.Access.WRITE_ONLY)
    String workingHoursVi;

    @Column(name = "working_hours_en", length = 255)
    @JsonProperty(value = "working_hours_en", access = JsonProperty.Access.WRITE_ONLY)
    String workingHoursEn;

    // --- Resolved / Backward Compatibility Getters & Setters ---

    @JsonProperty("name")
    public String getName() {
        return LanguageUtil.resolve(this.nameVi, this.nameEn);
    }

    public void setName(String name) {
        if (this.nameVi == null || this.nameVi.isBlank()) {
            this.nameVi = name;
        }
        if (this.nameEn == null || this.nameEn.isBlank()) {
            this.nameEn = name;
        }
    }

    @JsonProperty("description")
    public String getDescription() {
        return LanguageUtil.resolve(this.descriptionVi, this.descriptionEn);
    }

    public void setDescription(String description) {
        if (this.descriptionVi == null || this.descriptionVi.isBlank()) {
            this.descriptionVi = description;
        }
        if (this.descriptionEn == null || this.descriptionEn.isBlank()) {
            this.descriptionEn = description;
        }
    }

    @JsonProperty("working_hours")
    public String getWorkingHours() {
        return LanguageUtil.resolve(this.workingHoursVi, this.workingHoursEn);
    }

    public void setWorkingHours(String workingHours) {
        if (this.workingHoursVi == null || this.workingHoursVi.isBlank()) {
            this.workingHoursVi = workingHours;
        }
        if (this.workingHoursEn == null || this.workingHoursEn.isBlank()) {
            this.workingHoursEn = workingHours;
        }
    }

    @JsonProperty("requirements")
    public List<String> getRequirements() {
        return LanguageUtil.resolve(this.requirementsVi, this.requirementsEn);
    }

    public void setRequirements(List<String> requirements) {
        if (this.requirementsVi == null || this.requirementsVi.isEmpty()) {
            this.requirementsVi = requirements != null ? new ArrayList<>(requirements) : new ArrayList<>();
        }
        if (this.requirementsEn == null || this.requirementsEn.isEmpty()) {
            this.requirementsEn = requirements != null ? new ArrayList<>(requirements) : new ArrayList<>();
        }
    }

    @JsonProperty("benefits")
    public List<String> getBenefits() {
        return LanguageUtil.resolve(this.benefitsVi, this.benefitsEn);
    }

    public void setBenefits(List<String> benefits) {
        if (this.benefitsVi == null || this.benefitsVi.isEmpty()) {
            this.benefitsVi = benefits != null ? new ArrayList<>(benefits) : new ArrayList<>();
        }
        if (this.benefitsEn == null || this.benefitsEn.isEmpty()) {
            this.benefitsEn = benefits != null ? new ArrayList<>(benefits) : new ArrayList<>();
        }
    }

    @PrePersist
    public void handleBeforeCreate() {
        if (this.nameVi == null && this.nameEn != null) this.nameVi = this.nameEn;
        if (this.nameEn == null && this.nameVi != null) this.nameEn = this.nameVi;
        if (this.descriptionVi == null && this.descriptionEn != null) this.descriptionVi = this.descriptionEn;
        if (this.descriptionEn == null && this.descriptionVi != null) this.descriptionEn = this.descriptionVi;
        if (this.workingHoursVi == null && this.workingHoursEn != null) this.workingHoursVi = this.workingHoursEn;
        if (this.workingHoursEn == null && this.workingHoursVi != null) this.workingHoursEn = this.workingHoursVi;
        if ((this.requirementsVi == null || this.requirementsVi.isEmpty()) && (this.requirementsEn != null && !this.requirementsEn.isEmpty())) {
            this.requirementsVi = new ArrayList<>(this.requirementsEn);
        }
        if ((this.requirementsEn == null || this.requirementsEn.isEmpty()) && (this.requirementsVi != null && !this.requirementsVi.isEmpty())) {
            this.requirementsEn = new ArrayList<>(this.requirementsVi);
        }
        if ((this.benefitsVi == null || this.benefitsVi.isEmpty()) && (this.benefitsEn != null && !this.benefitsEn.isEmpty())) {
            this.benefitsVi = new ArrayList<>(this.benefitsEn);
        }
        if ((this.benefitsEn == null || this.benefitsEn.isEmpty()) && (this.benefitsVi != null && !this.benefitsVi.isEmpty())) {
            this.benefitsEn = new ArrayList<>(this.benefitsVi);
        }

        if (this.active == null) {
            this.active = true;
        }
        this.createdBy = SecurityUtil.getCurrentUser().orElse("");
        this.createdAt = Instant.now();
    }

    @PreUpdate
    public void handleBeforeUpdate() {
        if (this.nameVi == null && this.nameEn != null) this.nameVi = this.nameEn;
        if (this.nameEn == null && this.nameVi != null) this.nameEn = this.nameVi;
        if (this.descriptionVi == null && this.descriptionEn != null) this.descriptionVi = this.descriptionEn;
        if (this.descriptionEn == null && this.descriptionVi != null) this.descriptionEn = this.descriptionVi;
        if (this.workingHoursVi == null && this.workingHoursEn != null) this.workingHoursVi = this.workingHoursEn;
        if (this.workingHoursEn == null && this.workingHoursVi != null) this.workingHoursEn = this.workingHoursVi;

        this.updatedBy = SecurityUtil.getCurrentUser().orElse("");
        this.updatedAt = Instant.now();
    }
}
