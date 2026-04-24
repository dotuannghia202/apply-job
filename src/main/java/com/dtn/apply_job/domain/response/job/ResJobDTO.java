package com.dtn.apply_job.domain.response.job;

import com.dtn.apply_job.util.constant.enums.LevelEnum;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;
import java.util.Set;

@Setter
@Getter
public class ResJobDTO {

    private long id;
    private String name;
    private String location;
    private Double salary;
    private int quantity;
    private Set<LevelEnum> levels;
    private String description;

    private Instant startDate;

    private Instant endDate;
    private boolean active;

    private Instant createdAt;
    private String createdBy;

    private Instant updatedAt;
    private String updatedBy;
    private List<String> skills;

    private CompanyInfo company;
    private SpecializationInfo specialization;

    // Inner Classes
    @Getter
    @Setter
    public static class CompanyInfo {
        private long id;
        private String name;
        private String logo;
    }

    @Getter
    @Setter
    public static class SpecializationInfo {
        private long id;
        private String name;
    }
}
