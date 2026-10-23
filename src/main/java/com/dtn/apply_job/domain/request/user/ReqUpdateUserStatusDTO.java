package com.dtn.apply_job.domain.request.user;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReqUpdateUserStatusDTO {
    @NotNull(message = "Trạng thái không được để trống!")
    Boolean isActive;
}