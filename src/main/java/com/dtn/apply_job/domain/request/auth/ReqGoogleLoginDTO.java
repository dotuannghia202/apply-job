package com.dtn.apply_job.domain.request.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReqGoogleLoginDTO {

    @NotBlank(message = "Authorization code cannot be left blank")
    String code;

    String redirectUri;
}
