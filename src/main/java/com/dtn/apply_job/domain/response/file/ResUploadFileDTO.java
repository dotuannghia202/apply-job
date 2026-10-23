package com.dtn.apply_job.domain.response.file;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ResUploadFileDTO {
    String filePath;
    Instant uploadedAt;
}
