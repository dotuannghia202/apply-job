package com.dtn.apply_job.domain.request.resume;

import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReqUpdateResumeDTO {


    @Size(min = 1, message = "Tên tệp tin không được để trống!")
    String fileName;

    @Size(min = 1, message = "Đường dẫn tệp tin không được để trống!")
    String fileUrl;

    Boolean isActive;

    UUID specializationId;

    List<UUID> skillIds;
}