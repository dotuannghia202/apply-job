package com.dtn.apply_job.service;

import com.dtn.apply_job.common.response.ResultPaginationDTO;
import com.dtn.apply_job.domain.Notification;
import com.dtn.apply_job.domain.User;
import com.dtn.apply_job.domain.response.notification.ResNotificationDTO;
import com.dtn.apply_job.exception.IdInvalidException;
import com.dtn.apply_job.repository.NotificationRepository;
import com.dtn.apply_job.repository.UserRepository;
import com.dtn.apply_job.security.SecurityUtil;
import com.dtn.apply_job.util.constant.enums.ERole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate; // Công cụ bắn WebSocket
    private final UserRepository userRepository;

    public NotificationService(NotificationRepository notificationRepository,
                               SimpMessagingTemplate messagingTemplate,
                               UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.messagingTemplate = messagingTemplate;
        this.userRepository = userRepository;
    }

    // 1. Bắn thông báo cho CÁ NHÂN (Employer)
    public void sendToUser(User user, String title, String message, String type, Long refId, ERole targetRole) {
        Notification notif = new Notification();
        notif.setRecipient(user);
        notif.setTitle(title);
        notif.setMessage(message);
        notif.setType(type);
        notif.setReferenceId(refId);

        notif.setTargetRole(targetRole); // 👉 GẮN NHÃN Ở ĐÂY

        notificationRepository.save(notif);

        // Bắn WebSocket (Gửi luôn cả DTO để FE nhận diện)
        ResNotificationDTO dto = convertToDTO(notif);
        messagingTemplate.convertAndSend("/topic/user/" + user.getId(), dto);
    }

    // 2. Bắn thông báo cho TOÀN BỘ ADMIN
    public void sendToAllAdmins(String title, String message, String type) {
        // Lấy danh sách tất cả Admin
        List<User> admins = userRepository.findByRoles_Name(ERole.ADMIN);

        for (User admin : admins) {
            // Lưu DB cho từng Admin
            Notification notif = new Notification();
            notif.setRecipient(admin);
            notif.setTitle(title);
            notif.setMessage(message);
            notif.setType(type); // 👉 GẮN TYPE VÀO ĐÂY
            notif.setTargetRole(ERole.ADMIN); // Gắn nhãn cho Admin
            notificationRepository.save(notif);
        }

        // Bắn WebSocket vào kênh chung của Admin
        Notification wsNotif = new Notification();
        wsNotif.setTitle(title);
        wsNotif.setMessage(message);
        wsNotif.setType(type); // 👉 GẮN TYPE VÀO WEBSOCKET ĐỂ FRONTEND NHẬN DIỆN
        messagingTemplate.convertAndSend("/topic/admin", wsNotif);
    }

    // API Lấy danh sách thông báo
// Sửa lại hàm này, thêm tham số ERole role
    public ResultPaginationDTO getMyNotifications(Boolean isRead, Pageable pageable, ERole role) throws Exception {
        String email = SecurityUtil.getCurrentUser()
                .orElseThrow(() -> new IdInvalidException("Vui lòng đăng nhập!"));
        User currentUser = userRepository.findByEmail(email);

        Page<Notification> pageNotif;
        if (isRead == null) {
            // Lấy tất cả thông báo THEO ROLE
            pageNotif = notificationRepository.findByRecipient_IdAndTargetRoleOrderByCreatedAtDesc(
                    currentUser.getId(), role, pageable);
        } else {
            // Lọc theo trạng thái Chưa đọc (isRead = false) hoặc Đã đọc THEO ROLE
            pageNotif = notificationRepository.findByRecipient_IdAndTargetRoleAndIsReadOrderByCreatedAtDesc(
                    currentUser.getId(), role, isRead, pageable);
        }

        // Ép sang ResNotificationDTO thông qua hàm convertToDTO vừa tạo
        List<ResNotificationDTO> listDTO = pageNotif.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        // Đóng gói ResultPaginationDTO chuẩn xác
        ResultPaginationDTO resultPaginationDTO = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();

        meta.setPage(pageNotif.getNumber() + 1);
        meta.setPageSize(pageNotif.getSize());
        meta.setPages(pageNotif.getTotalPages());
        meta.setTotal(pageNotif.getTotalElements());

        resultPaginationDTO.setMeta(meta);
        resultPaginationDTO.setResult(listDTO);

        return resultPaginationDTO;
    }

    private ResNotificationDTO convertToDTO(Notification notification) {
        ResNotificationDTO dto = new ResNotificationDTO();
        dto.setId(notification.getId());
        dto.setTitle(notification.getTitle());
        dto.setMessage(notification.getMessage());
        dto.setRead(notification.isRead());
        dto.setCreatedAt(notification.getCreatedAt());

        dto.setType(notification.getType());
        dto.setReferenceId(notification.getReferenceId());
        return dto;
    }

    // 2. ĐÁNH DẤU 1 THÔNG BÁO LÀ "ĐÃ ĐỌC" (CÓ CHỐNG HACK)
    // =======================================================
    @Transactional
    public void markAsRead(Long notifId) throws Exception {
        // 1. Lấy thông báo từ DB
        Notification notif = notificationRepository.findById(notifId)
                .orElseThrow(() -> new IdInvalidException("Thông báo không tồn tại!"));

        // 2. Lấy Email người đang gọi API
        String email = SecurityUtil.getCurrentUser()
                .orElseThrow(() -> new IdInvalidException("Vui lòng đăng nhập!"));

        // 3. RÀO CẢN BẢO MẬT (IDOR): Kiểm tra thông báo này có đúng là của người đang đăng nhập không?
        if (!notif.getRecipient().getEmail().equals(email)) {
            throw new AccessDeniedException("Lỗi bảo mật: Bạn không có quyền thay đổi thông báo của người khác!");
        }

        // 4. Nếu hợp lệ, đánh dấu đã đọc và lưu
        notif.setRead(true);
        notificationRepository.save(notif);
    }

    // =======================================================
    // 3. ĐÁNH DẤU "ĐÃ ĐỌC TẤT CẢ" THÔNG BÁO CỦA MÌNH
    // =======================================================

    @Transactional
    public void markAllAsRead(ERole role) throws Exception {
        String email = SecurityUtil.getCurrentUser()
                .orElseThrow(() -> new IdInvalidException("Vui lòng đăng nhập!"));
        User currentUser = userRepository.findByEmail(email);

        // Lấy danh sách toàn bộ thông báo CHƯA ĐỌC của User này THEO ROLE
        List<Notification> unreadNotifications = notificationRepository.findByRecipient_IdAndTargetRoleAndIsReadFalse(
                currentUser.getId(), role);

        if (!unreadNotifications.isEmpty()) {
            for (Notification n : unreadNotifications) {
                n.setRead(true);
            }
            notificationRepository.saveAll(unreadNotifications);
        }
    }
}