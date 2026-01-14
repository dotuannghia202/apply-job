package com.dtn.apply_job.domain;

import com.dtn.apply_job.util.SecurityUtil;
import com.dtn.apply_job.util.constant.enums.GenderEnum;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

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

    private int age;

    @NotNull
    @Enumerated(EnumType.STRING)
    private GenderEnum gender;

    private String address;
    private String refreshToken;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss a", timezone = "GMT+7")
    private Instant createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss a", timezone = "GMT+7")
    private Instant updatedAt;

    private String createdBy;
    private String updatedBy;

    @ManyToOne
    @JoinColumn(name = "company_id")
    private Company company;

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

    public User(String name, String password, String email, int age, GenderEnum gender, String address, String refreshToken) {
        this.name = name;
        this.password = password;
        this.email = email;
        this.age = age;
        this.gender = gender;
        this.address = address;
        this.refreshToken = refreshToken;
    }

}
