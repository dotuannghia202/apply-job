package com.dtn.apply_job.domain.request.resume;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReqCreateResumeDTO {

    @NotBlank(message = "Tên tệp tin không được để trống")
    String fileName;

    @NotBlank(message = "Đường dẫn tệp tin không được để trống")
    String fileUrl;

    UUID specializationId;

    List<UUID> skillIds;

    
}