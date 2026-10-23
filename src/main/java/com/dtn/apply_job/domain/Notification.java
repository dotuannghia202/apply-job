package com.dtn.apply_job.domain;

import com.dtn.apply_job.util.LanguageUtil;
import com.dtn.apply_job.util.constant.enums.ERole;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    // Gửi cho ai? (Người nhận)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false)
    User recipient;

    @Column(name = "title_vi")
    @JsonProperty("title_vi")
    String titleVi;

    @Column(name = "title_en")
    @JsonProperty("title_en")
    String titleEn;

    @Column(name = "message_vi", columnDefinition = "TEXT")
    @JsonProperty("message_vi")
    String messageVi;

    @Column(name = "message_en", columnDefinition = "TEXT")
    @JsonProperty("message_en")
    String messageEn;

    String type;

    // ID của đối tượng liên quan (VD: ID của Đơn ứng tuyển, ID của Công ty). Có thể null.
    UUID referenceId;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_role")
    ERole targetRole;

    @Builder.Default
    boolean isRead = false; // Đã đọc hay chưa?

    @Builder.Default
    @Column(updatable = false)
    Instant createdAt = Instant.now();

    @JsonProperty("title")
    public String getTitle() {
        return LanguageUtil.resolve(this.titleVi, this.titleEn);
    }

    public void setTitle(String title) {
        if (this.titleVi == null || this.titleVi.isBlank()) {
            this.titleVi = title;
        }
        if (this.titleEn == null || this.titleEn.isBlank()) {
            this.titleEn = title;
        }
    }

    @JsonProperty("message")
    public String getMessage() {
        return LanguageUtil.resolve(this.messageVi, this.messageEn);
    }

    public void setMessage(String message) {
        if (this.messageVi == null || this.messageVi.isBlank()) {
            this.messageVi = message;
        }
        if (this.messageEn == null || this.messageEn.isBlank()) {
            this.messageEn = message;
        }
    }

    @PrePersist
    public void handleBeforeCreate() {
        if (this.titleVi == null && this.titleEn != null) this.titleVi = this.titleEn;
        if (this.titleEn == null && this.titleVi != null) this.titleEn = this.titleVi;
        if (this.messageVi == null && this.messageEn != null) this.messageVi = this.messageEn;
        if (this.messageEn == null && this.messageVi != null) this.messageEn = this.messageVi;
        if (this.createdAt == null) this.createdAt = Instant.now();
    }
}