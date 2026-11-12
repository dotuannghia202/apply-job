package com.dtn.apply_job.domain;

import com.dtn.apply_job.security.SecurityUtil;
import com.dtn.apply_job.util.LanguageUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
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
@Table(name = "industries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Industry {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @Column(name = "name_vi")
    @JsonProperty(value = "name_vi", access = JsonProperty.Access.WRITE_ONLY)
    String nameVi;

    @Column(name = "name_en")
    @JsonProperty(value = "name_en", access = JsonProperty.Access.WRITE_ONLY)
    String nameEn;

    // 1 Ngành lớn có nhiều Mảng ngành (Specialization)
    @OneToMany(mappedBy = "industry", fetch = FetchType.LAZY)
    @JsonIgnore
    List<Specialization> specializations;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss a", timezone = "GMT+7")
    Instant createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss a", timezone = "GMT+7")
    Instant updatedAt;

    String createdBy;
    String updatedBy;

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

    @PrePersist
    public void handleBeforeCreate() {
        if (this.nameVi == null && this.nameEn != null) this.nameVi = this.nameEn;
        if (this.nameEn == null && this.nameVi != null) this.nameEn = this.nameVi;
        this.createdBy = SecurityUtil.getCurrentUser().orElse("");
        this.createdAt = Instant.now();
    }

    @PreUpdate
    public void handleBeforeUpdate() {
        if (this.nameVi == null && this.nameEn != null) this.nameVi = this.nameEn;
        if (this.nameEn == null && this.nameVi != null) this.nameEn = this.nameVi;
        this.updatedBy = SecurityUtil.getCurrentUser().orElse("");
        this.updatedAt = Instant.now();
    }
}