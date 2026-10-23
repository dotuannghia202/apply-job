package com.dtn.apply_job.domain;

import com.dtn.apply_job.security.SecurityUtil;
import com.dtn.apply_job.util.constant.enums.GenderEnum;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class User {

    @Column(name = "google_refresh_token", length = 500)
    String googleRefreshToken;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @NotBlank(message = "Full name is not blank")
    String name;

    @NotBlank(message = "Password is not blank")
    String password;

    @NotBlank(message = "Email is not blank")
    String email;

    String avatarUrl;

    Integer age;

    @Enumerated(EnumType.STRING)
    GenderEnum gender;

    String address;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    Boolean isActive = true;

    @Column(columnDefinition = "TEXT")
    String refreshToken;

    Instant createdAt;

    Instant updatedAt;

    String createdBy;
    String updatedBy;

    @ManyToOne
    @JoinColumn(name = "company_id")
    Company company;

    @Builder.Default
    @NotEmpty(message = "A user must have at least one role.")
    @ManyToMany(fetch = FetchType.EAGER) // Tải quyền ngay lập tức khi đăng nhập
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    Set<Role> roles = new HashSet<>();

    @Builder.Default
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "saved_jobs", // Hibernate sẽ tự động tạo bảng này
            joinColumns = @JoinColumn(name = "candidate_id"),
            inverseJoinColumns = @JoinColumn(name = "job_id")
    )
    Set<Job> savedJobs = new HashSet<>();

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
