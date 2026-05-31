package com.dtn.apply_job.repository;

import com.dtn.apply_job.domain.Notification;
import com.dtn.apply_job.util.constant.enums.ERole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long>, JpaSpecificationExecutor<Notification> {
    Page<Notification> findByRecipient_IdAndTargetRoleOrderByCreatedAtDesc(Long recipientId, ERole targetRole, Pageable pageable);

    Page<Notification> findByRecipient_IdAndTargetRoleAndIsReadOrderByCreatedAtDesc(Long recipientId, ERole targetRole, boolean isRead, Pageable pageable);

    long countByRecipient_IdAndIsReadFalse(Long recipientId);

    List<Notification> findByRecipient_IdAndTargetRoleAndIsReadFalse(Long recipientId, ERole targetRole);
}
