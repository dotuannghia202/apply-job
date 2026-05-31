package com.dtn.apply_job.domain.response.notification;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

// Tạo file ResNotificationDTO.java
@Getter
@Setter
public class ResNotificationDTO {
    private Long id;
    private String title;
    private String message;
    private boolean isRead;
    private Instant createdAt;
    private String type;

    // ID của đối tượng liên quan (VD: ID của Đơn ứng tuyển, ID của Công ty). Có thể null.
    private Long referenceId;
}