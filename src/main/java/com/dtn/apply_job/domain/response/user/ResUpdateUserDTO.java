package com.dtn.apply_job.domain.response.user;

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
public class ResUpdateUserDTO {
    UUID id;
    String name;
    String email;
    String avatarUrl;
    Integer age;
    String gender;
    String address;
    String preferredLanguage;
    List<String> roles;
    Boolean isActive;
    Instant updatedAt;
    String updatedBy;

    CompanyUser company;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class CompanyUser {
        UUID id;
        String name;
    }
}
