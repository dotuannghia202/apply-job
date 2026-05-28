package com.dtn.apply_job.domain.response.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Setter
@Getter
public class ResUserDTO {
    private long id;
    private String name;
    private String email;
    private String avatarUrl;
    private Integer age;
    private String gender;
    private String address;

    private Instant createdAt;
    private String createdBy;
    private Instant updatedAt;
    private String updatedBy;

    @JsonProperty("isActive")
    private boolean isActive;

    private CompanyUser company;

    private List<String> roles;

    @Setter
    @Getter
    public static class CompanyUser {
        private Long id;
        String name;
        String logo;
    }

}
