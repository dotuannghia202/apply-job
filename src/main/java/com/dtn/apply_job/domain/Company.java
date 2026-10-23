package com.dtn.apply_job.domain;

import com.dtn.apply_job.security.SecurityUtil;
import com.dtn.apply_job.util.LanguageUtil;
import com.dtn.apply_job.util.constant.enums.CompanyStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "companies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @NotBlank(message = "Field name is required!")
    String name;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    CompanyStatus status = CompanyStatus.PENDING;

    @Column(name = "description_vi", columnDefinition = "TEXT")
    @JsonProperty("description_vi")
    String descriptionVi;

    @Column(name = "description_en", columnDefinition = "TEXT")
    @JsonProperty("description_en")
    String descriptionEn;

    @NotBlank(message = "Field address is required!")
    String address;

    String logo;

    Instant createdAt;

    Instant updatedAt;

    String createdBy;

    String updatedBy;

    @OneToMany(mappedBy = "company", fetch = FetchType.LAZY)
    @JsonIgnore
    List<User> users;

    @OneToMany(mappedBy = "company", fetch = FetchType.LAZY)
    @JsonIgnore
    List<Job> jobs;

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

    @PrePersist
    public void handleBeforeCreate() {
        if (this.descriptionVi == null && this.descriptionEn != null) this.descriptionVi = this.descriptionEn;
        if (this.descriptionEn == null && this.descriptionVi != null) this.descriptionEn = this.descriptionVi;
        this.createdBy = SecurityUtil.getCurrentUser().isPresent() ? SecurityUtil.getCurrentUser().get() : "";
        this.createdAt = Instant.now();
    }

    @PreUpdate
    public void handleBeforeUpdate() {
        if (this.descriptionVi == null && this.descriptionEn != null) this.descriptionVi = this.descriptionEn;
        if (this.descriptionEn == null && this.descriptionVi != null) this.descriptionEn = this.descriptionVi;
        this.updatedBy = SecurityUtil.getCurrentUser().isPresent() ? SecurityUtil.getCurrentUser().get() : "";
        this.updatedAt = Instant.now();
    }
}
