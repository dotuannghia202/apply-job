package com.dtn.apply_job.domain.response.user;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Setter
@Getter
public class ResUserDTO {
    private long id;
    private String name;
    private String email;
    private int age;
    private String gender;
    private String address;

    private Instant createdAt;
    private String createdBy;
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
