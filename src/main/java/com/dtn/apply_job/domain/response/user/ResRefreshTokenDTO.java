package com.dtn.apply_job.domain.response.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class ResRefreshTokenDTO {
    private String accessToken;
}
