package com.dtn.apply_job.domain.response.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ResLoginDTO {

    @JsonProperty("user")
    UserLogin userLogin;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class UserLogin {
        UUID id;
        String email;
        String name;
        List<String> roles;
        String avatarUrl;
        Boolean isGmailLinked;
        String preferredLanguage;
        CompanyInfo company;

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        @FieldDefaults(level = AccessLevel.PRIVATE)
        public static class CompanyInfo {
            UUID id;
            String name;
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class UserGetAccount {
        UserLogin user;
    }
}
