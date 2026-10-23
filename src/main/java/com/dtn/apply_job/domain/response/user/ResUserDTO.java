package com.dtn.apply_job.domain.response.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ResUserDTO {
    UUID id;
    String name;
    String email;
    String avatarUrl;
    Integer age;
    String gender;
    String address;

    Instant createdAt;
    String createdBy;
    Instant updatedAt;
    String updatedBy;

    @JsonProperty("isActive")
    boolean isActive;

    CompanyUser company;

    List<String> roles;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class CompanyUser {
        UUID id;
        String name;
        String logo;
    }
}
