package com.dtn.apply_job.domain.response.user;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Setter
@Getter
public class ResUpdateUserDTO {
    private long id;
    private String name;
    private String email;
    private Integer age;
    private String gender;
    private String address;
    private List<String> roles;
    private Boolean isActive;
    private Instant updatedAt;
    private String updatedBy;

    private CompanyUser company;

    @Setter
    @Getter
    public static class CompanyUser {
        private long id;
        String name;
    }
}
