package com.dtn.apply_job.domain.response.notification;

import com.dtn.apply_job.util.LanguageUtil;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ResNotificationDTO {
    UUID id;

    @JsonProperty("title_vi")
    String titleVi;

    @JsonProperty("title_en")
    String titleEn;

    @JsonProperty("message_vi")
    String messageVi;

    @JsonProperty("message_en")
    String messageEn;

    boolean isRead;
    Instant createdAt;
    String type;

    // ID của đối tượng liên quan (VD: ID của Đơn ứng tuyển, ID của Công ty). Có thể null.
    UUID referenceId;

    @JsonProperty("title")
    public String getTitle() {
        return LanguageUtil.resolve(this.titleVi, this.titleEn);
    }

    public void setTitle(String title) {
        if (this.titleVi == null) this.titleVi = title;
        if (this.titleEn == null) this.titleEn = title;
    }

    @JsonProperty("message")
    public String getMessage() {
        return LanguageUtil.resolve(this.messageVi, this.messageEn);
    }

    public void setMessage(String message) {
        if (this.messageVi == null) this.messageVi = message;
        if (this.messageEn == null) this.messageEn = message;
    }
}