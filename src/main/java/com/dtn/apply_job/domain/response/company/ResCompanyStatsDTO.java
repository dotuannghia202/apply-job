package com.dtn.apply_job.domain.response.company;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ResCompanyStatsDTO {
    long totalCompanies;
    long pendingApproval;
    long approved;
}