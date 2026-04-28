package com.dtn.apply_job.domain;

import com.dtn.apply_job.security.SecurityUtil;
import com.dtn.apply_job.util.constant.enums.GenderEnum;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotBlank(message = "Full name is not blank")
    private String name;

    @NotBlank(message = "Password is not blank")
    private String password;

    @NotBlank(message = "Email is not blank")
    private String email;

    private Integer age;


    @Enumerated(EnumType.STRING)
    private GenderEnum gender;

    private String address;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(columnDefinition = "TEXT")
    private String refreshToken;


    private Instant createdAt;


    private Instant updatedAt;

    private String createdBy;
    private String updatedBy;

    @ManyToOne
    @JoinColumn(name = "company_id")
    private Company company;


    @ManyToMany(fetch = FetchType.EAGER) // Tải quyền ngay lập tức khi đăng nhập
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

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


    public User() {

    }

    public User(String name, String password, String email, int age, GenderEnum gender, String address, Boolean isActive, String refreshToken) {
        this.name = name;
        this.password = password;
        this.email = email;
        this.age = age;
        this.isActive = isActive;
        this.gender = gender;
        this.address = address;
        this.refreshToken = refreshToken;
    }

}
