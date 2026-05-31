package com.dtn.apply_job.domain;

import com.dtn.apply_job.util.constant.enums.ERole;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "notifications")
@Getter
@Setter
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Gửi cho ai? (Người nhận)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String message;

    private String type;

    // ID của đối tượng liên quan (VD: ID của Đơn ứng tuyển, ID của Công ty). Có thể null.
    private Long referenceId;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_role")
    private ERole targetRole;

    private boolean isRead = false; // Đã đọc hay chưa?

    @Column(updatable = false)
    private Instant createdAt = Instant.now();
}