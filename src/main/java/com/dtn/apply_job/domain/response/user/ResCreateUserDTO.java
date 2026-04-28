package com.dtn.apply_job.domain.response.user;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Setter
@Getter
public class ResCreateUserDTO {
    private long id;
    private String email;
    private String name;
    private int age;
    private String gender;
    private String address;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss a", timezone = "GMT+7")
    private Instant createdAt;

    private String createdBy;

    private CompanyUser company;

    private List<String> roles;

    @Setter
    @Getter
    public static class CompanyUser {
        private long id;
        String name;
    }
}
